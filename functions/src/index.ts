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
