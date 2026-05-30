import * as admin from "firebase-admin";
import { logger } from "firebase-functions";
import { onDocumentCreated } from "firebase-functions/v2/firestore";

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
          statusType,
        },
        android: {
          priority: "high",
          notification: {
            channelId: "kalkan_alerts",
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
