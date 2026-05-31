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

interface SafetyStatusData {
  uid?: string;
  displayName?: string;
  statusType?: string;
}

const FCM_BATCH_SIZE = 500;

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

  const dataPayload: Record<string, string> = {
    type: "announcement",
    announcementId,
    priority,
  };

  const androidPriority = priority === "urgent" ? "high" : "normal";

  for (let offset = 0; offset < tokens.length; offset += FCM_BATCH_SIZE) {
    const batch = tokens.slice(offset, offset + FCM_BATCH_SIZE);

    try {
      const response = await messaging.sendEachForMulticast({
        tokens: batch,
        notification: { title, body },
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
  const usersSnapshot = await admin.firestore()
    .collection("users")
    .where("familyGroupId", "==", familyGroupId)
    .get();
  const tokens = new Set<string>();

  usersSnapshot.forEach((doc) => {
    if (doc.id === sourceUid) {
      return;
    }

    const rawToken = doc.data().fcmToken;
    if (typeof rawToken === "string" && rawToken.trim()) {
      tokens.add(rawToken.trim());
    }
  });

  return Array.from(tokens);
}

async function sendSafetyAlertToTokens(
  tokens: string[],
  title: string,
  body: string,
  statusId: string,
  sourceUid: string,
  statusType: string,
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
          type: "family_safety_alert",
          statusId,
          sourceUid,
          senderUid: sourceUid,
          statusType,
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
      title,
      message,
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

    const displayName = (data.displayName ?? "").trim() || "Bir aile üyeniz";
    const title = statusType === "sos" ? "ACİL SOS" : "YARDIM İSTİYOR";
    const body = statusType === "sos"
      ? `${displayName} acil SOS çağrısı gönderdi.`
      : `${displayName} yardım istiyor.`;
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
  const settingsRef = admin.firestore().collection("system_settings").doc("earthquake_monitor");
  const settingsDoc = await settingsRef.get();
  
  let settings = {
    enabled: true,
    intervalMinutes: 5,
    minSystemMagnitude: 2.0,
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
        minSystemMagnitude: typeof data.minSystemMagnitude === "number" ? data.minSystemMagnitude : 2.0,
        lastCheckedAt: data.lastCheckedAt || null,
        lastProcessedEarthquakeId: data.lastProcessedEarthquakeId || null
      };
    }
  }

  if (!settings.enabled) {
    logger.info("AFAD monitor skipped: disabled in system settings.");
    return;
  }

  const now = admin.firestore.Timestamp.now();
  if (settings.lastCheckedAt) {
    const lastCheckedMs = settings.lastCheckedAt.toDate().getTime();
    const elapsedMin = (Date.now() - lastCheckedMs) / (1000 * 60);
    if (elapsedMin < settings.intervalMinutes - 0.1) {
      logger.info("AFAD monitor skipped: interval not reached.", { elapsedMin, interval: settings.intervalMinutes });
      return;
    }
  }

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

  for (const eq of reversedEarthquakes) {
    const eventID = String(eq.eventID || "").trim();
    if (!eventID) continue;

    const eventRef = admin.firestore().collection("earthquake_events").doc(eventID);
    const eventDoc = await eventRef.get();

    if (eventDoc.exists) {
      continue; // already processed
    }

    // Save to database
    const occurredAtDate = new Date(eq.date + " GMT+0300");
    const eventData = {
      source: "AFAD",
      earthquakeId: eventID,
      magnitude: Number(eq.magnitude || 0),
      location: String(eq.location || "Bilinmeyen Bölge"),
      depthKm: eq.depth ? Number(eq.depth) : null,
      occurredAt: admin.firestore.Timestamp.fromDate(occurredAtDate),
      latitude: eq.latitude ? Number(eq.latitude) : null,
      longitude: eq.longitude ? Number(eq.longitude) : null,
      createdAt: admin.firestore.Timestamp.now(),
      notificationSent: false,
      notificationSentAt: null as admin.firestore.Timestamp | null
    };

    await eventRef.set(eventData);
    logger.info("New earthquake detected and saved.", { eventID, location: eventData.location, magnitude: eventData.magnitude });

    // Retrieve eligible users (only where notifications are enabled and magnitude is above threshold)
    let usersSnapshot;
    try {
      usersSnapshot = await admin.firestore().collection("users")
        .where("earthquakeNotificationsEnabled", "==", true)
        .where("earthquakeNotificationMinMagnitude", "<=", eventData.magnitude)
        .get();
    } catch (err) {
      logger.warn("Optimized query failed (missing index). Falling back to in-memory filtering.", err);
      usersSnapshot = await admin.firestore().collection("users")
        .where("earthquakeNotificationsEnabled", "==", true)
        .get();
    }

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
    if (tokenList.length > 0) {
      const title = "Deprem Bildirimi";
      const body = `${eventData.location} yakınlarında ${eventData.magnitude} büyüklüğünde deprem meydana geldi.`;
      
      const payload = {
        type: "earthquake",
        earthquakeId: eventID,
        magnitude: String(eventData.magnitude),
        location: eventData.location,
        source: "AFAD"
      };

      let successCount = 0;
      let failureCount = 0;

      for (let offset = 0; offset < tokenList.length; offset += FCM_BATCH_SIZE) {
        const batch = tokenList.slice(offset, offset + FCM_BATCH_SIZE);
        try {
          const response = await admin.messaging().sendEachForMulticast({
            tokens: batch,
            notification: { title, body },
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

      logger.info("Earthquake FCM notifications sent.", { eventID, totalTokens: tokenList.length, successCount, failureCount });
    }

    // Update event document with notification status
    await eventRef.update({
      notificationSent: true,
      notificationSentAt: admin.firestore.Timestamp.now()
    });

    // Update settings lastProcessedEarthquakeId
    await settingsRef.update({
      lastProcessedEarthquakeId: eventID
    });
  }
});
