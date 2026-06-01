import * as admin from "firebase-admin";
import { logger } from "firebase-functions";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";

admin.initializeApp();

type TargetAudience = "all" | "guests" | "registered";

interface AnnouncementData {
  title?: string;
  message?: string;
  targetAudience?: string;
  priority?: string;
  status?: string;
}

interface EmergencyAlertData {
  title?: string;
  body?: string;
  region?: string;
  priority?: string;
  target?: string;
  source?: string;
  status?: string;
}

interface SafetyStatusData {
  uid?: string;
  displayName?: string;
  statusType?: string;
}

interface FamilyCheckRequestData {
  familyGroupId?: string;
  groupId?: string;
  requestedByUid?: string;
  requesterUid?: string;
  requestedByName?: string;
  requesterName?: string;
  status?: string;
  createdAt?: number | admin.firestore.Timestamp;
}

const FCM_BATCH_SIZE = 500;

// ─────────────────────────────────────────────────────────────
// Notification Text Helpers
// ─────────────────────────────────────────────────────────────

function safeUserName(displayName: string | null | undefined): string {
  const name = (displayName ?? "").trim();
  return name.length > 0 ? name : "Bir aile üyesi";
}

function safeLocation(location: string | null | undefined): string {
  const loc = (location ?? "").trim();
  return loc.length > 0 ? loc : "";
}

function buildEarthquakeBody(location: string, magnitude: number): string {
  const loc = safeLocation(location);
  if (loc.length === 0) {
    return `Türkiye genelinde ${magnitude} büyüklüğünde deprem kaydedildi.`;
  }
  return `${loc} yakınlarında ${magnitude} büyüklüğünde deprem meydana geldi.`;
}

function nowIso(): string {
  return new Date().toISOString();
}

function isGuestUser(email: unknown): boolean {
  if (email === null || email === undefined) {
    return true;
  }
  if (typeof email !== "string") {
    return true;
  }
  return email.trim().length === 0;
}

function normalizeTargetAudience(value: string | undefined): TargetAudience {
  if (value === "guests" || value === "registered") {
    return value;
  }
  return "all";
}

async function collectEligibleTokens(targetAudience: TargetAudience): Promise<string[]> {
  const usersSnapshot = await admin.firestore().collection("users").get();
  const tokens = new Set<string>();

  usersSnapshot.forEach((doc) => {
    const data = doc.data();
    const rawToken = data.fcmToken;
    if (typeof rawToken !== "string") {
      return;
    }

    const token = rawToken.trim();
    if (!token) {
      return;
    }

    const guest = isGuestUser(data.email);
    const matches = (() => {
      switch (targetAudience) {
        case "all":
          return true;
        case "guests":
          return guest;
        case "registered":
          return !guest;
        default:
          return false;
      }
    })();

    if (matches) {
      tokens.add(token);
    }
  });

  return Array.from(tokens);
}

async function sendAnnouncementToTokens(
  tokens: string[],
  title: string,
  body: string,
  announcementId: string,
  priority: string,
): Promise<{ successCount: number; failureCount: number }> {
  const messaging = admin.messaging();
  let successCount = 0;
  let failureCount = 0;

  const notifTitle = "KALKAN Duyurusu";
  const notifBody = title.trim() || body.trim();

  const dataPayload: Record<string, string> = {
    type: "announcement",
    title: notifTitle,
    body: notifBody,
    announcementId,
    priority,
    source: "KALKAN",
    createdAt: nowIso(),
  };

  const androidPriority = priority === "urgent" ? "high" : "normal";

  for (let offset = 0; offset < tokens.length; offset += FCM_BATCH_SIZE) {
    const batch = tokens.slice(offset, offset + FCM_BATCH_SIZE);

    try {
      const response = await messaging.sendEachForMulticast({
        tokens: batch,
        notification: { title: notifTitle, body: notifBody },
        data: dataPayload,
        android: {
          priority: androidPriority,
          notification: {
            channelId: "kalkan_alerts",
            clickAction: "com.zgrcan.kalkan.NOTIFICATION_CLICK",
          },
        },
      });

      successCount += response.successCount;
      failureCount += response.failureCount;

      response.responses.forEach((result, index) => {
        if (!result.success) {
          logger.warn("FCM token send failed", {
            announcementId,
            tokenIndex: offset + index,
            errorCode: result.error?.code,
            errorMessage: result.error?.message,
          });
        }
      });
    } catch (error) {
      failureCount += batch.length;
      logger.error("FCM multicast batch failed", {
        announcementId,
        batchSize: batch.length,
        error,
      });
    }
  }

  return { successCount, failureCount };
}

async function collectFamilyTokens(familyGroupId: string, sourceUid: string): Promise<string[]> {
  const { tokens } = await collectFamilyGroupPushTargets(familyGroupId, sourceUid);
  return tokens;
}

/** Aile push: üye uid'lerini members + users.familyGroupId üzerinden toplar, fcmToken users/{uid}'den okunur. */
async function collectFamilyGroupPushTargets(
  familyGroupId: string,
  excludeUid: string,
): Promise<{ tokens: string[]; memberCount: number }> {
  const memberUids = new Set<string>();

  const membersSnapshot = await admin.firestore()
    .collection("family_groups")
    .doc(familyGroupId)
    .collection("members")
    .get();

  membersSnapshot.forEach((doc) => {
    if (doc.id !== excludeUid) {
      memberUids.add(doc.id);
    }
  });

  const usersSnapshot = await admin.firestore()
    .collection("users")
    .where("familyGroupId", "==", familyGroupId)
    .get();

  usersSnapshot.forEach((doc) => {
    if (doc.id !== excludeUid) {
      memberUids.add(doc.id);
    }
  });

  const tokens = new Set<string>();
  for (const uid of memberUids) {
    const userDoc = await admin.firestore().collection("users").doc(uid).get();
    if (!userDoc.exists) {
      continue;
    }
    const rawToken = userDoc.get("fcmToken");
    if (typeof rawToken === "string" && rawToken.trim()) {
      tokens.add(rawToken.trim());
    }
  }

  return { tokens: Array.from(tokens), memberCount: memberUids.size };
}

async function sendSafetyAlertToTokens(
  tokens: string[],
  title: string,
  body: string,
  statusId: string,
  sourceUid: string,
  statusType: string,
  familyGroupId: string,
): Promise<{ successCount: number; failureCount: number }> {
  const messaging = admin.messaging();
  let successCount = 0;
  let failureCount = 0;

  const notifType = statusType === "sos" ? "sos_alert" : "help_request";

  for (let offset = 0; offset < tokens.length; offset += FCM_BATCH_SIZE) {
    const batch = tokens.slice(offset, offset + FCM_BATCH_SIZE);

    try {
      const response = await messaging.sendEachForMulticast({
        tokens: batch,
        notification: { title, body },
        data: {
          type: notifType,
          title,
          body,
          safetyStatusId: statusId,
          sourceUid,
          senderUid: sourceUid,
          statusType,
          familyGroupId,
          source: "KALKAN",
          createdAt: nowIso(),
        },
        android: {
          priority: "high",
          notification: {
            channelId: "kalkan_alerts",
            clickAction: "com.zgrcan.kalkan.NOTIFICATION_CLICK",
          },
        },
      });

      successCount += response.successCount;
      failureCount += response.failureCount;
    } catch (error) {
      failureCount += batch.length;
      logger.error("Family safety alert multicast batch failed", {
        statusId,
        batchSize: batch.length,
        error,
      });
    }
  }

  return { successCount, failureCount };
}

export const sendAnnouncementPush = onDocumentCreated(
  "announcements/{announcementId}",
  async (event) => {
    const announcementId = event.params.announcementId;
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn("Announcement snapshot missing", { announcementId });
      return;
    }

    const data = snapshot.data() as AnnouncementData;
    const status = data.status ?? "";

    if (status !== "published") {
      logger.info("Skipping push: status is not published", {
        announcementId,
        status,
      });
      return;
    }

    const title = (data.title ?? "").trim();
    const message = (data.message ?? "").trim();

    if (!title || !message) {
      logger.warn("Skipping push: empty title or message", { announcementId });
      return;
    }

    const targetAudience = normalizeTargetAudience(data.targetAudience);
    const priority = (data.priority ?? "normal").trim() || "normal";

    // Professional announcement title
    const pushTitle = "KALKAN Duyurusu";
    const pushBody = title.trim() || message.trim();

    const tokens = await collectEligibleTokens(targetAudience);
    const tokenCount = tokens.length;

    logger.info("Announcement push started", {
      announcementId,
      targetAudience,
      tokenCount,
    });

    if (tokenCount === 0) {
      logger.info("Announcement push skipped: no eligible tokens", {
        announcementId,
        targetAudience,
        tokenCount: 0,
        successCount: 0,
        failureCount: 0,
      });
      return;
    }

    const { successCount, failureCount } = await sendAnnouncementToTokens(
      tokens,
      pushTitle,
      pushBody,
      announcementId,
      priority,
    );

    logger.info("Announcement push completed", {
      announcementId,
      targetAudience,
      tokenCount,
      successCount,
      failureCount,
    });
  },
);

async function sendFamilyCheckRequestToTokens(
  tokens: string[],
  title: string,
  body: string,
  requestId: string,
  familyGroupId: string,
  requestedByUid: string,
  requestedByName: string,
  createdAt: string,
): Promise<{ successCount: number; failureCount: number }> {
  const messaging = admin.messaging();
  let successCount = 0;
  let failureCount = 0;

  for (let offset = 0; offset < tokens.length; offset += FCM_BATCH_SIZE) {
    const batch = tokens.slice(offset, offset + FCM_BATCH_SIZE);

    try {
      const response = await messaging.sendEachForMulticast({
        tokens: batch,
        notification: { title, body },
        data: {
          type: "family_check_request",
          familyGroupId,
          requestedByUid,
          requestedByName,
          createdAt,
          requestId,
          source: "KALKAN",
        },
        android: {
          priority: "high",
          notification: {
            channelId: "kalkan_alerts",
            clickAction: "com.zgrcan.kalkan.NOTIFICATION_CLICK",
          },
        },
      });

      successCount += response.successCount;
      failureCount += response.failureCount;
    } catch (error) {
      failureCount += batch.length;
      logger.error("Family check request multicast batch failed", {
        requestId,
        batchSize: batch.length,
        error,
      });
    }
  }

  return { successCount, failureCount };
}

export const sendFamilyCheckRequestPush = onDocumentCreated(
  {
    document: "family_check_requests/{requestId}",
    region: "europe-west1",
  },
  async (event) => {
    const requestId = event.params.requestId;
    logger.info("Family check request push function started", { requestId });

    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("Family check request snapshot missing", { requestId });
      return;
    }

    const data = snapshot.data() as FamilyCheckRequestData;
    logger.info("Family check request loaded", {
      requestId,
      status: data.status ?? null,
      familyGroupId: data.familyGroupId ?? data.groupId ?? null,
      requestedByUid: data.requestedByUid ?? data.requesterUid ?? null,
      requestedByName: data.requestedByName ?? data.requesterName ?? null,
      createdAt: data.createdAt ?? null,
    });

    const status = (data.status ?? "sent").trim();
    if (status !== "sent") {
      logger.info("Family check request skipped: status is not sent", { requestId, status });
      return;
    }

    const familyGroupId = (data.familyGroupId ?? data.groupId ?? "").trim();
    const requestedByUid = (data.requestedByUid ?? data.requesterUid ?? "").trim();
    const requestedByName = safeUserName(data.requestedByName ?? data.requesterName);

    if (!familyGroupId || !requestedByUid) {
      logger.warn("Family check request skipped: missing group or requester", {
        requestId,
        familyGroupId,
        requestedByUid,
      });
      return;
    }

    const { tokens, memberCount } = await collectFamilyGroupPushTargets(
      familyGroupId,
      requestedByUid,
    );

    logger.info("Family check request targets resolved", {
      requestId,
      familyGroupId,
      requestedByUid,
      familyMembersCount: memberCount,
      tokensCount: tokens.length,
    });

    if (tokens.length === 0) {
      logger.info("Family check request skipped: no eligible FCM tokens", {
        requestId,
        familyGroupId,
        requestedByUid,
        familyMembersCount: memberCount,
      });
      return;
    }

    const title = "Aile Durum Kontrolü";
    const body = `${requestedByName}, güvenlik durumunuzu paylaşmanızı istiyor.`;
    const createdAt =
      typeof data.createdAt === "number"
        ? new Date(data.createdAt).toISOString()
        : data.createdAt instanceof admin.firestore.Timestamp
          ? data.createdAt.toDate().toISOString()
          : nowIso();

    const { successCount, failureCount } = await sendFamilyCheckRequestToTokens(
      tokens,
      title,
      body,
      requestId,
      familyGroupId,
      requestedByUid,
      requestedByName,
      createdAt,
    );

    logger.info("Family check request push completed", {
      requestId,
      familyGroupId,
      requestedByUid,
      familyMembersCount: memberCount,
      tokensCount: tokens.length,
      successCount,
      failureCount,
    });
  },
);

export const sendFamilySafetyAlertPush = onDocumentCreated(
  "safety_status/{statusId}",
  async (event) => {
    const statusId = event.params.statusId;
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn("Safety status snapshot missing", { statusId });
      return;
    }

    const data = snapshot.data() as SafetyStatusData;
    const sourceUid = (data.uid ?? "").trim();
    const statusType = (data.statusType ?? "").trim();

    if (!sourceUid || (statusType !== "sos" && statusType !== "need_help")) {
      return;
    }

    const userSnapshot = await admin.firestore().collection("users").doc(sourceUid).get();
    const familyGroupId = userSnapshot.get("familyGroupId");
    if (typeof familyGroupId !== "string" || !familyGroupId.trim()) {
      logger.info("Family safety alert skipped: user has no family group", {
        statusId,
        sourceUid,
      });
      return;
    }

    const displayName = safeUserName(data.displayName);
    const title = statusType === "sos" ? "SOS Bildirimi" : "Yardım Talebi";
    const body = statusType === "sos"
      ? `${displayName} acil yardım çağrısı gönderdi. Konum bilgisi mevcutsa haritadan kontrol edin.`
      : `${displayName} yardım talebi gönderdi. Aile güvenlik ekranından durumunu kontrol edin.`;
    const tokens = await collectFamilyTokens(familyGroupId, sourceUid);

    if (tokens.length === 0) {
      logger.info("Family safety alert skipped: no eligible tokens", {
        statusId,
        sourceUid,
        familyGroupId,
      });
      return;
    }

    const { successCount, failureCount } = await sendSafetyAlertToTokens(
      tokens,
      title,
      body,
      statusId,
      sourceUid,
      statusType,
      familyGroupId,
    );

    logger.info("Family safety alert push completed", {
      statusId,
      sourceUid,
      familyGroupId,
      tokenCount: tokens.length,
      successCount,
      failureCount,
    });
  },
);

export const monitorEarthquakes = onSchedule("*/1 * * * *", async (event) => {
  logger.info("Function started: monitorEarthquakes");

  const settingsRef = admin.firestore().collection("system_settings").doc("earthquake_monitor");
  const settingsDoc = await settingsRef.get();
  
  let settings = {
    enabled: true,
    intervalMinutes: 5,
    minSystemMagnitude: 4.0,
    lastCheckedAt: null as admin.firestore.Timestamp | null,
    lastProcessedEarthquakeId: null as string | null
  };

  if (!settingsDoc.exists) {
    await settingsRef.set(settings);
  } else {
    const data = settingsDoc.data();
    if (data) {
      settings = {
        enabled: typeof data.enabled === "boolean" ? data.enabled : true,
        intervalMinutes: typeof data.intervalMinutes === "number" ? data.intervalMinutes : 5,
        minSystemMagnitude: typeof data.minSystemMagnitude === "number" ? data.minSystemMagnitude : 4.0,
        lastCheckedAt: data.lastCheckedAt || null,
        lastProcessedEarthquakeId: data.lastProcessedEarthquakeId || null
      };
    }
  }

  logger.info("Settings loaded", { settings });

  if (!settings.enabled) {
    logger.info("Interval skipped: disabled in system settings.");
    return;
  }

  const now = admin.firestore.Timestamp.now();
  if (settings.lastCheckedAt) {
    const lastCheckedMs = settings.lastCheckedAt.toDate().getTime();
    const elapsedMin = (Date.now() - lastCheckedMs) / (1000 * 60);
    if (elapsedMin < settings.intervalMinutes - 0.1) {
      logger.info("Interval skipped: interval not reached.", { elapsedMin, interval: settings.intervalMinutes });
      return;
    }
  }

  logger.info("Interval not skipped. Proceeding with AFAD check.");

  // Mark checking now
  await settingsRef.update({ lastCheckedAt: now });

  // 24 hours ago in Turkey time (UTC+3)
  const turkeyNow = new Date(Date.now() + 3 * 60 * 60 * 1000);
  const turkeyStart = new Date(turkeyNow.getTime() - 24 * 60 * 60 * 1000);

  const formatDate = (d: Date) => {
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())} ${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:${pad(d.getUTCSeconds())}`;
  };

  const startStr = formatDate(turkeyStart);
  const endStr = formatDate(turkeyNow);

  let earthquakes: any[] = [];
  try {
    const url = `https://deprem.afad.gov.tr/apiv2/event/filter?start=${encodeURIComponent(startStr)}&end=${encodeURIComponent(endStr)}&minmag=${settings.minSystemMagnitude}&orderby=timedesc&limit=100&format=json`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    if (Array.isArray(data)) {
      earthquakes = data;
    }
    logger.info("AFAD fetched count", { count: earthquakes.length, url });
  } catch (err: any) {
    logger.error("AFAD service fetch failed", { error: err?.message || err });
    return;
  }

  if (earthquakes.length === 0) {
    logger.info("No earthquakes found above the threshold.");
    return;
  }

  // Process in chronological order (oldest first)
  const reversedEarthquakes = [...earthquakes].reverse();
  logger.info("Target earthquake list count", { count: reversedEarthquakes.length });

  for (const eq of reversedEarthquakes) {
    const eventID = String(eq.eventID || "").trim();
    if (!eventID) continue;

    const eventRef = admin.firestore().collection("earthquake_events").doc(eventID);
    const eventDoc = await eventRef.get();

    if (eventDoc.exists) {
      logger.info("Duplicate skipped.", { eventID });
      continue; // already processed
    }

    // Fix AFAD date format (e.g. "2024-05-31 16:20:00" -> "2024-05-31T16:20:00+03:00")
    const dateStr = String(eq.date || "").replace(" ", "T") + "+03:00";
    const occurredAtDate = new Date(dateStr);
    
    const magnitude = Number(eq.magnitude || 0);
    const location = String(eq.location || "Bilinmeyen Bölge");

    logger.info("New earthquake detected.", { eventID, location, magnitude, dateStr });

    const eventData = {
      source: "AFAD",
      earthquakeId: eventID,
      magnitude: magnitude,
      location: location,
      depthKm: eq.depth ? Number(eq.depth) : null,
      occurredAt: isNaN(occurredAtDate.getTime()) ? admin.firestore.Timestamp.now() : admin.firestore.Timestamp.fromDate(occurredAtDate),
      latitude: eq.latitude ? Number(eq.latitude) : null,
      longitude: eq.longitude ? Number(eq.longitude) : null,
      createdAt: admin.firestore.Timestamp.now(),
      notificationSent: false,
      notificationSentAt: null as admin.firestore.Timestamp | null
    };

    await eventRef.set(eventData);

    // Retrieve eligible users safely without composite index
    let usersSnapshot;
    try {
      usersSnapshot = await admin.firestore().collection("users")
        .where("earthquakeNotificationsEnabled", "==", true)
        .get();
    } catch (err) {
      logger.error("Failed to query eligible users", { error: err });
      continue;
    }

    logger.info("Eligible users count (enabled=true)", { count: usersSnapshot.size });

    const tokens = new Set<string>();
    usersSnapshot.forEach((doc) => {
      const u = doc.data();
      const token = u.fcmToken;
      if (typeof token === "string" && token.trim()) {
        const minMag = u.earthquakeNotificationMinMagnitude;
        if (minMag === null || minMag === undefined || Number(minMag) <= eventData.magnitude) {
          tokens.add(token.trim());
        }
      }
    });

    const tokenList = Array.from(tokens);
    logger.info("Collected token count (filtered by magnitude)", { count: tokenList.length });

    if (tokenList.length > 0) {
      const notifTitle = "Deprem Bildirimi";
      const notifBody = buildEarthquakeBody(eventData.location, eventData.magnitude);

      const payload: Record<string, string> = {
        type: "earthquake",
        title: notifTitle,
        body: notifBody,
        earthquakeId: eventID,
        magnitude: String(eventData.magnitude),
        location: safeLocation(eventData.location) || "Bilinmiyor",
        source: "AFAD",
        createdAt: nowIso(),
      };
      if (eventData.depthKm !== null && eventData.depthKm !== undefined) {
        payload["depthKm"] = String(eventData.depthKm);
      }
      if (eventData.latitude !== null && eventData.latitude !== undefined) {
        payload["latitude"] = String(eventData.latitude);
      }
      if (eventData.longitude !== null && eventData.longitude !== undefined) {
        payload["longitude"] = String(eventData.longitude);
      }

      let successCount = 0;
      let failureCount = 0;

      for (let offset = 0; offset < tokenList.length; offset += FCM_BATCH_SIZE) {
        const batch = tokenList.slice(offset, offset + FCM_BATCH_SIZE);
        try {
          const response = await admin.messaging().sendEachForMulticast({
            tokens: batch,
            notification: { title: notifTitle, body: notifBody },
            data: payload,
            android: {
              priority: "high",
              notification: {
                channelId: "kalkan_alerts",
                clickAction: "com.zgrcan.kalkan.NOTIFICATION_CLICK",
              },
            },
          });
          successCount += response.successCount;
          failureCount += response.failureCount;
        } catch (fcmErr) {
          failureCount += batch.length;
          logger.error("FCM multicast batch error", fcmErr);
        }
      }

      logger.info("FCM stats for earthquake", { eventID, successCount, failureCount });
    }

    // Update event document with notification status
    await eventRef.update({
      notificationSent: true,
      notificationSentAt: admin.firestore.Timestamp.now()
    });
    logger.info("notificationSent updated to true.", { eventID });

    // Update settings lastProcessedEarthquakeId
    await settingsRef.update({
      lastProcessedEarthquakeId: eventID
    });
  }
});

export const sendEmergencyAlertPush = onDocumentCreated(
  "emergency_alerts/{alertId}",
  async (event) => {
    const alertId = event.params.alertId;
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn("Emergency alert snapshot missing", { alertId });
      return;
    }

    const data = snapshot.data() as EmergencyAlertData;
    const status = data.status ?? "";

    if (status !== "published") {
      logger.info("Skipping push: status is not published", {
        alertId,
        status,
      });
      return;
    }

    const title = (data.title ?? "").trim();
    const body = (data.body ?? "").trim();

    if (!title || !body) {
      logger.warn("Skipping push: empty title or body", { alertId });
      return;
    }

    const priority = (data.priority ?? "Önemli").trim();
    const region = (data.region ?? "Tüm Türkiye").trim();
    const target = data.target ?? "all_users";

    const pushTitle = "Acil Durum Uyarısı";
    const pushBody = `${title} - ${body}`;

    // Use existing collectEligibleTokens logic
    const tokens = await collectEligibleTokens("all");
    const tokenCount = tokens.length;

    logger.info("Emergency alert push started", {
      alertId,
      target,
      tokenCount,
    });

    if (tokenCount === 0) {
      logger.info("Emergency alert push skipped: no eligible tokens", { alertId });
      return;
    }

    const dataPayload: Record<string, string> = {
      type: "emergency_alert",
      alertId,
      title,
      body,
      priority,
      region,
      target,
      source: "admin_panel",
      createdAt: nowIso(),
    };

    const messaging = admin.messaging();
    let successCount = 0;
    let failureCount = 0;

    for (let offset = 0; offset < tokens.length; offset += FCM_BATCH_SIZE) {
      const batch = tokens.slice(offset, offset + FCM_BATCH_SIZE);

      try {
        const response = await messaging.sendEachForMulticast({
          tokens: batch,
          notification: { title: pushTitle, body: pushBody },
          data: dataPayload,
          android: {
            priority: "high",
            notification: {
              channelId: "kalkan_alerts",
              clickAction: "com.zgrcan.kalkan.NOTIFICATION_CLICK",
            },
          },
        });

        successCount += response.successCount;
        failureCount += response.failureCount;
      } catch (error) {
        failureCount += batch.length;
        logger.error("FCM multicast batch failed for emergency alert", {
          alertId,
          batchSize: batch.length,
          error,
        });
      }
    }

    logger.info("Emergency alert push completed", {
      alertId,
      tokenCount,
      successCount,
      failureCount,
    });
  },
);
