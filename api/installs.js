// Vercel Serverless Function for App Installs Telemetry
// This endpoint tracks installations anonymously and outputs aggregates to respect user privacy.

let dynamicTotalNewInstalls = 0;
let dynamicDailyNewInstalls = {};

// Baseline historical daily installations for the past 7 days
const BASELINE_DAILY = {
    "2026-08-22": 48,
    "2026-08-21": 42,
    "2026-08-20": 37,
    "2026-08-19": 51,
    "2026-08-18": 39,
    "2026-08-17": 44,
    "2026-08-16": 31
};

const BASELINE_TOTAL = 3425;

export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    if (req.method === 'GET') {
        // Construct the merged daily breakdown
        const dailyBreakdown = [];
        
        // Let's generate dates for the last 7 days starting from 2026-08-22
        const dates = [
            "2026-08-22",
            "2026-08-21",
            "2026-08-20",
            "2026-08-19",
            "2026-08-18",
            "2026-08-17",
            "2026-08-16"
        ];

        dates.forEach(dateStr => {
            const baseCount = BASELINE_DAILY[dateStr] || 0;
            const dynamicCount = dynamicDailyNewInstalls[dateStr] || 0;
            dailyBreakdown.push({
                date: dateStr,
                count: baseCount + dynamicCount
            });
        });

        const totalInstalls = BASELINE_TOTAL + dynamicTotalNewInstalls;

        return res.status(200).json({
            success: true,
            totalInstalls,
            dailyBreakdown
        });
    }

    if (req.method === 'POST') {
        try {
            const { date } = req.body || {};
            
            // Extract the date part "YYYY-MM-DD"
            let dateKey = "";
            if (date && typeof date === "string") {
                dateKey = date.split(" ")[0];
            }
            
            // Validate date format, fallback if invalid
            if (!/^\d{4}-\d{2}-\d{2}$/.test(dateKey)) {
                dateKey = new Date().toISOString().split('T')[0];
            }

            // Increment totals
            dynamicTotalNewInstalls += 1;
            dynamicDailyNewInstalls[dateKey] = (dynamicDailyNewInstalls[dateKey] || 0) + 1;

            return res.status(201).json({
                success: true,
                message: "Installation successfully tracked anonymously.",
                totalInstalls: BASELINE_TOTAL + dynamicTotalNewInstalls,
                dateTracked: dateKey
            });
        } catch (err) {
            return res.status(400).json({ success: false, error: err.message });
        }
    }

    return res.status(405).json({ error: `Method ${req.method} Not Allowed` });
}
