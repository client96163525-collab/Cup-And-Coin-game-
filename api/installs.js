// Vercel Serverless Function for App Installs Tracking
let installs = [
    { id: "1", username: "Aarav_M", device: "Google Pixel 8 (Android 15)", version: "1.0.4", date: "2026-08-22 12:30:15", ip: "192.168.x.x" },
    { id: "2", username: "Priya_Sharma", device: "Samsung Galaxy S24 (Android 14)", version: "1.0.4", date: "2026-08-22 10:15:42", ip: "10.0.x.x" },
    { id: "3", username: "Rahul_Kumar", device: "OnePlus 12 (Android 14)", version: "1.0.4", date: "2026-08-21 22:45:10", ip: "172.16.x.x" },
    { id: "4", username: "Sneha_Patel", device: "Xiaomi 14 Pro (Android 14)", version: "1.0.3", date: "2026-08-21 18:20:05", ip: "192.168.1.x" },
    { id: "5", username: "Vikram_Singh", device: "Motorola Edge 50 (Android 14)", version: "1.0.4", date: "2026-08-20 15:10:33", ip: "10.1.x.x" }
];

export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    if (req.method === 'GET') {
        return res.status(200).json({ success: true, count: installs.length, installs });
    }

    if (req.method === 'POST') {
        try {
            const { username, device, version, date } = req.body || {};
            const newInstall = {
                id: Date.now().toString(),
                username: username || `User_${Math.floor(Math.random() * 9000 + 1000)}`,
                device: device || "Android Device",
                version: version || "1.0.4",
                date: date || new Date().toISOString().replace('T', ' ').substring(0, 19),
                ip: req.headers['x-forwarded-for'] || req.socket.remoteAddress || 'Unknown'
            };

            installs.unshift(newInstall);
            // Keep last 100 installs
            if (installs.length > 100) installs.pop();

            return res.status(201).json({ success: true, message: "Install registered successfully", install: newInstall });
        } catch (err) {
            return res.status(400).json({ success: false, error: err.message });
        }
    }

    return res.status(405).json({ error: `Method ${req.method} Not Allowed` });
}
