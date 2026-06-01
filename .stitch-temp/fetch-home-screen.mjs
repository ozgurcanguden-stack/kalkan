/**
 * Stitch ana sayfa HTML + ekran görüntüsü indirir.
 * Kullanım (PowerShell):
 *   $env:STITCH_API_KEY = "your-key"
 *   node fetch-home-screen.mjs
 */
import { stitch } from "@google/stitch-sdk";
import { writeFile, mkdir } from "node:fs/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const PROJECT_ID = "13405266281914185324";
const SCREEN_ID = "77a05c6fb65c4bf69ce8957af291478b";
const __dirname = dirname(fileURLToPath(import.meta.url));
const outDir = join(__dirname, "downloads");

if (!process.env.STITCH_API_KEY) {
  console.error("STITCH_API_KEY ortam değişkeni gerekli.");
  process.exit(1);
}

const project = stitch.project(PROJECT_ID);
const screen = await project.getScreen(SCREEN_ID);
const htmlUrl = await screen.getHtml();
const imageUrl = await screen.getImage();

await mkdir(outDir, { recursive: true });

const htmlRes = await fetch(htmlUrl);
const html = await htmlRes.text();
await writeFile(join(outDir, "ana-sayfa-112.html"), html, "utf8");

const imgRes = await fetch(imageUrl);
const imgBuf = Buffer.from(await imgRes.arrayBuffer());
await writeFile(join(outDir, "ana-sayfa-112.png"), imgBuf);

console.log("HTML:", join(outDir, "ana-sayfa-112.html"));
console.log("PNG:", join(outDir, "ana-sayfa-112.png"));
