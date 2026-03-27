import { useState } from "react";

const C = {
  primary: "#6C5CE7", primaryDark: "#5A4BD1", primaryLight: "#F0EEFF",
  bg: "#FAFAFE", white: "#FFFFFF", text: "#1A1A2E", textSec: "#6B7089",
  textMuted: "#9B9DB7", border: "#E8E8F0", borderLight: "#F0F0F6",
  green: "#00B894", greenLight: "#E8FBF5", orange: "#F39C12", orangeLight: "#FEF6E7",
  red: "#E74C3C", redLight: "#FDE8E8",
};

/* ── Shared UI ─────────────────────────────── */
const Btn = ({ children, primary, outline, small, ghost, disabled, onClick, style }) => (
  <button onClick={disabled ? undefined : onClick} style={{
    background: disabled ? "#D0D0DD" : primary ? C.primary : "transparent",
    color: disabled ? "#fff" : primary ? "#fff" : outline ? C.primary : ghost ? C.textMuted : C.textSec,
    border: outline ? `1.5px solid ${C.primary}` : primary ? "none" : ghost ? "none" : `1px solid ${C.border}`,
    borderRadius: 8, padding: small ? "6px 14px" : "10px 22px",
    fontSize: small ? 12 : 13, fontWeight: 600, cursor: disabled ? "not-allowed" : "pointer",
    display: "inline-flex", alignItems: "center", gap: 6, opacity: disabled ? 0.6 : 1, ...style,
  }}>{children}</button>
);

const Chip = ({ children, selected, onClick }) => (
  <span onClick={onClick} style={{
    display: "inline-block", padding: "8px 18px", borderRadius: 20, fontSize: 13, fontWeight: 500,
    background: selected ? C.primaryLight : C.white, color: selected ? C.primary : C.textSec,
    border: `1.5px solid ${selected ? C.primary : C.border}`, cursor: "pointer", userSelect: "none",
  }}>{selected ? "✓ " : ""}{children}</span>
);

const Tag = ({ children, color = C.primaryDark, bg = C.primaryLight }) => (
  <span style={{ background: bg, padding: "2px 8px", borderRadius: 4, fontSize: 11, color, fontWeight: 500 }}>{children}</span>
);

/* Persistent privacy link — clickable, expands inline */
const PrivacyLink = () => {
  const [open, setOpen] = useState(false);
  return (
    <div style={{ marginTop: 12 }}>
      <div onClick={() => setOpen(!open)} style={{ fontSize: 11, color: C.primary, cursor: "pointer", fontWeight: 600, display: "inline-flex", alignItems: "center", gap: 4 }}>
        🔒 What does HR see? {open ? "▲" : "→"}
      </div>
      {open && (
        <div style={{
          marginTop: 6, padding: "10px 14px", borderRadius: 8,
          background: C.primaryLight, fontSize: 12, color: C.primaryDark, lineHeight: 1.6,
        }}>
          <strong>HR sees:</strong> attendance, goal status, practice completion rate.<br />
          <strong>HR does NOT see:</strong> session content, reflections, or messages with your expert.
        </div>
      )}
    </div>
  );
};

/* Privacy message — consistent everywhere */
const PRIVACY_TEXT = "HR sees: attendance, goal status, practice completion rate. HR does NOT see: session content, reflections, or messages with your expert.";

const Sidebar = () => (
  <div style={{
    width: 200, background: C.white, borderRight: `1px solid ${C.border}`,
    padding: "16px 10px", display: "flex", flexDirection: "column", gap: 2, flexShrink: 0,
  }}>
    <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "4px 14px 16px", borderBottom: `1px solid ${C.borderLight}`, marginBottom: 8 }}>
      <div style={{ width: 28, height: 28, borderRadius: 6, background: C.primary, display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontSize: 11, fontWeight: 800 }}>TL</div>
      <div>
        <div style={{ fontSize: 14, fontWeight: 700, color: C.text }}>TopLeader</div>
        <div style={{ fontSize: 10, color: C.textMuted }}>Participant</div>
      </div>
    </div>
    {["🏠 Dashboard", "📅 Sessions", "🎓 Coaches", "💬 Get Feedback", "📖 Self-Development"].map((item, i) => (
      <div key={i} style={{
        padding: "9px 14px", borderRadius: 8, fontSize: 13.5,
        fontWeight: i === 0 ? 600 : 400,
        background: i === 0 ? C.primaryLight : "transparent",
        color: i === 0 ? C.primary : C.textSec, cursor: "pointer",
      }}>{item}</div>
    ))}
    <div style={{ borderTop: `1px solid ${C.borderLight}`, margin: "10px 0 6px" }} />
    {["✉️ Messages", "📝 Notes"].map((item, i) => (
      <div key={i} style={{ padding: "9px 14px", borderRadius: 8, fontSize: 13.5, color: C.textSec, cursor: "pointer" }}>{item}</div>
    ))}
    <div style={{ marginTop: "auto", borderTop: `1px solid ${C.borderLight}`, paddingTop: 8 }}>
      <div style={{ padding: "9px 14px", fontSize: 13.5, color: C.textSec, cursor: "pointer" }}>⚙️ Settings</div>
    </div>
  </div>
);

const ProgramBanner = ({ is180 }) => (
  <div style={{
    background: C.white, border: `1px solid ${C.border}`, borderRadius: 12,
    padding: "16px 20px", marginBottom: 12,
  }}>
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 8 }}>
      <div style={{ fontSize: 11, fontWeight: 600, color: C.textMuted, textTransform: "uppercase", letterSpacing: 0.5 }}>
        Leadership Development {is180 ? "2026" : "Q1 2026"}
      </div>
      {is180 && <Tag bg={C.primaryLight} color={C.primary}>Cycle 1 of 2</Tag>}
    </div>
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <span style={{ fontSize: 14, fontWeight: 700, color: C.text }}>{is180 ? "Cycle 1 · " : ""}Week 6 of 13</span>
        <div style={{ width: 120, height: 5, background: C.borderLight, borderRadius: 3 }}>
          <div style={{ width: "46%", height: "100%", background: C.primary, borderRadius: 3 }} />
        </div>
        <span style={{ fontSize: 12, fontWeight: 600, color: C.primary }}>46%</span>
      </div>
      <div style={{ textAlign: "right" }}>
        <div style={{ fontSize: 11, color: C.textMuted }}>Focus</div>
        <div style={{ fontSize: 13, fontWeight: 600, color: C.primary }}>Giving feedback</div>
      </div>
    </div>
    {is180 && <div style={{ fontSize: 11, color: C.textMuted, marginTop: 6 }}>180-day program · 1 cycle remaining</div>}
  </div>
);

const GoalContext = () => {
  const [editing, setEditing] = useState(false);
  return (
    <div style={{ marginBottom: 12 }}>
      {!editing ? (
        <div style={{
          display: "flex", alignItems: "center", gap: 8,
          padding: "8px 16px",
          background: C.bg, border: `1px solid ${C.borderLight}`, borderRadius: 8, fontSize: 13,
        }}>
          <span style={{ fontSize: 11, fontWeight: 700, color: C.textMuted, textTransform: "uppercase", letterSpacing: 0.3, flexShrink: 0 }}>My Goal</span>
          <span style={{ color: C.border }}>|</span>
          <span style={{ color: C.text, flex: 1 }}>Give specific positive feedback after at least 2 meetings per week</span>
          <span onClick={() => setEditing(true)} style={{ fontSize: 11, color: C.primary, fontWeight: 600, cursor: "pointer", flexShrink: 0 }}>Edit</span>
        </div>
      ) : (
        <div style={{
          padding: "14px 16px",
          background: C.white, border: `1.5px solid ${C.primary}`, borderRadius: 8,
        }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: C.text }}>Edit your personal goal</span>
            <span onClick={() => setEditing(false)} style={{ fontSize: 11, color: C.textMuted, cursor: "pointer" }}>Cancel</span>
          </div>
          <textarea
            defaultValue="Give specific positive feedback after at least 2 meetings per week"
            style={{
              width: "100%", padding: "10px 12px", border: `1px solid ${C.border}`, borderRadius: 6,
              fontSize: 13, fontFamily: "inherit", resize: "vertical", minHeight: 50,
              color: C.text, boxSizing: "border-box",
            }}
          />
          <div style={{
            background: C.orangeLight, borderRadius: 6, padding: "8px 12px", marginTop: 8,
            fontSize: 11, color: C.orange, lineHeight: 1.5,
          }}>
            ⚠️ This week's practice won't change — it was already generated from your current goal. Your updated goal will be reflected in next week's practice (Monday).
          </div>
          <div style={{ fontSize: 11, color: C.textMuted, marginTop: 8, lineHeight: 1.5 }}>
            Want to change your focus area? Focus area stays fixed within a cycle to ensure consistent progress measurement. You can switch at the end of this cycle.
          </div>
          <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
            <Btn primary small onClick={() => setEditing(false)}>Save goal</Btn>
            <Btn ghost small onClick={() => setEditing(false)}>Cancel</Btn>
          </div>
        </div>
      )}
    </div>
  );
};

const WeekHistory = () => (
  <div style={{ marginTop: 14, paddingTop: 12, borderTop: `1px solid ${C.borderLight}`, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
    <div style={{ display: "flex", gap: 4 }}>
      {[{ s: "yes" }, { s: "yes" }, { s: "yes" }, { s: "yes" }, { s: "partial" }].map(({ s }, i) => (
        <div key={i} style={{
          width: 20, height: 20, borderRadius: 10, fontSize: 10, fontWeight: 600,
          display: "flex", alignItems: "center", justifyContent: "center",
          background: s === "yes" ? C.greenLight : C.orangeLight,
          color: s === "yes" ? C.green : C.orange,
        }}>{s === "yes" ? "✓" : "½"}</div>
      ))}
    </div>
    <span style={{ fontSize: 11, color: C.primary, fontWeight: 600, cursor: "pointer" }}>4 of 5 weeks completed →</span>
  </div>
);

/* ═══════════════════════════════════════════════
   SCREEN 1a: WELCOME — "Why am I here?"
   Context before enrollment. 10 seconds reading.
   ═══════════════════════════════════════════════ */
const Welcome = ({ onContinue }) => (
  <div style={{ padding: "32px 40px", maxWidth: 560 }}>
    <div style={{
      background: `linear-gradient(135deg, ${C.primary} 0%, ${C.primaryDark} 100%)`,
      borderRadius: 12, padding: "32px 28px", marginBottom: 28, color: "#fff", textAlign: "center",
    }}>
      <div style={{ fontSize: 32, marginBottom: 12 }}>👋</div>
      <div style={{ fontSize: 22, fontWeight: 700, marginBottom: 8 }}>Welcome to your development program</div>
      <div style={{ fontSize: 15, opacity: 0.9, lineHeight: 1.5 }}>Leadership Development Q1 2026</div>
    </div>

    <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "20px 24px", marginBottom: 20 }}>
      <div style={{ fontSize: 15, fontWeight: 700, color: C.text, marginBottom: 16 }}>What to expect</div>
      {[
        { icon: "🎯", title: "Program goal", desc: "Improve feedback culture & accountability for Team Leads" },
        { icon: "📅", title: "Duration", desc: "90 days · Mar 15 → Jun 13, 2026" },
        { icon: "🎓", title: "5 coaching sessions", desc: "With an expert you choose, every 2–3 weeks" },
        { icon: "✅", title: "Weekly practice", desc: "One small, concrete action each week. Under 1 minute to review." },
        { icon: "📊", title: "3 quick check-ins", desc: "Baseline, midpoint, and final — each under a minute. So you can see your own progress." },
      ].map((item, i) => (
        <div key={i} style={{ display: "flex", gap: 12, padding: "10px 0", borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none" }}>
          <span style={{ fontSize: 18, flexShrink: 0, marginTop: 2 }}>{item.icon}</span>
          <div>
            <div style={{ fontSize: 13, fontWeight: 600, color: C.text }}>{item.title}</div>
            <div style={{ fontSize: 12, color: C.textSec, lineHeight: 1.4 }}>{item.desc}</div>
          </div>
        </div>
      ))}
    </div>

    <div style={{ background: C.primaryLight, borderRadius: 8, padding: "12px 16px", marginBottom: 24, fontSize: 12, color: C.primaryDark, lineHeight: 1.6 }}>
      🔒 <strong>Your privacy:</strong> {PRIVACY_TEXT}
    </div>

    <Btn primary onClick={onContinue} style={{ padding: "12px 32px", fontSize: 14, width: "100%" }}>Let's get started →</Btn>
    <div style={{ fontSize: 11, color: C.textMuted, marginTop: 8, textAlign: "center" }}>Takes about 2 minutes to set up.</div>
  </div>
);

/* ═══════════════════════════════════════════════
   SCREEN 1b: ENROLLMENT
   Focus area + goal from AI suggestions
   ═══════════════════════════════════════════════ */
const Enrollment = () => {
  const [focus, setFocus] = useState("");
  const [showMore, setShowMore] = useState(false);
  const [goalMode, setGoalMode] = useState("suggestions"); // suggestions | custom
  const [selectedGoal, setSelectedGoal] = useState(null);
  const [customGoalText, setCustomGoalText] = useState("");

  // HR-recommended areas for this program (from wizard Step 1)
  const recommended = ["Giving feedback", "Delegation"];
  // Full library — all have assessment questions
  const moreAreas = ["Self-awareness", "Strategic thinking", "Team motivation", "Communication", "Conflict resolution", "Time management", "Emotional intelligence", "Decision making"];

  const suggestedGoals = {
    "Giving feedback": [
      "Give specific positive feedback after at least 2 meetings per week",
      "Address one performance issue per sprint with concrete examples",
      "Ask one team member for feedback on my leadership each week",
    ],
    "Delegation": [
      "Delegate at least 2 tasks per week and trust the outcome",
      "Brief my team clearly on expected outcomes instead of steps",
      "Resist taking back delegated work for 2 full weeks",
    ],
    "Self-awareness": [
      "Journal 3 sentences about my leadership decisions at end of each day",
      "Ask for honest feedback from 1 peer per week on my blind spots",
      "Notice and name my emotional reactions in 2 meetings per week",
    ],
  };
  // Fallback for areas without specific suggestions
  const defaultGoals = [
    "Practice one concrete behavior change in this area each week",
    "Notice and reflect on 2 situations related to this area per week",
    "Ask for feedback from one colleague on this topic each sprint",
  ];

  const goals = suggestedGoals[focus] || defaultGoals;

  const selectArea = (a) => { setFocus(a); setSelectedGoal(null); setGoalMode("suggestions"); };

  return (
    <div style={{ padding: "32px 40px", maxWidth: 600 }}>
      {/* Step 1: Focus area */}
      <div style={{ marginBottom: 28 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
          <div style={{ width: 22, height: 22, borderRadius: 11, background: C.primary, color: "#fff", fontSize: 11, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center" }}>1</div>
          <span style={{ fontSize: 14, fontWeight: 700, color: C.text }}>Choose your focus area</span>
        </div>
        <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 12, marginLeft: 30 }}>Pick 1 area you want to work on during this program. Choose carefully — your focus area stays fixed for the full 90-day cycle. You can always adjust your personal goal within it.</div>

        {/* HR-recommended areas */}
        <div style={{ marginLeft: 30, marginBottom: 8 }}>
          <div style={{ fontSize: 11, fontWeight: 600, color: C.primary, marginBottom: 6 }}>Recommended for this program</div>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
            {recommended.map(a => (
              <Chip key={a} selected={focus === a} onClick={() => selectArea(a)}>{a}</Chip>
            ))}
          </div>
        </div>

        {/* Expandable: full library */}
        <div style={{ marginLeft: 30 }}>
          {!showMore ? (
            <div onClick={() => setShowMore(true)} style={{
              fontSize: 12, color: C.primary, fontWeight: 600, cursor: "pointer", padding: "6px 0",
            }}>+ Show more areas ({moreAreas.length} available) →</div>
          ) : (
            <div>
              <div style={{ fontSize: 11, fontWeight: 600, color: C.textMuted, marginBottom: 6, marginTop: 4 }}>All available areas</div>
              <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
                {moreAreas.map(a => (
                  <Chip key={a} selected={focus === a} onClick={() => selectArea(a)}>{a}</Chip>
                ))}
              </div>
              <div onClick={() => setShowMore(false)} style={{
                fontSize: 11, color: C.textMuted, cursor: "pointer", marginTop: 6,
              }}>← Show less</div>
            </div>
          )}
        </div>
      </div>

      {/* Step 2: Personal goal — AI suggestions or custom */}
      <div style={{ marginBottom: 28 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
          <div style={{ width: 22, height: 22, borderRadius: 11, background: focus ? C.primary : C.borderLight, color: focus ? "#fff" : C.textMuted, fontSize: 11, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center" }}>2</div>
          <span style={{ fontSize: 14, fontWeight: 700, color: C.text }}>Set your personal goal</span>
        </div>
        <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 12, marginLeft: 30 }}>Pick one or write your own. This is what you'll work toward.</div>

        {focus && goalMode === "suggestions" && (
          <div style={{ marginLeft: 30 }}>
            {goals.map((g, i) => (
              <div key={i} onClick={() => setSelectedGoal(i)} style={{
                padding: "12px 16px", marginBottom: 8, borderRadius: 8, cursor: "pointer",
                border: `1.5px solid ${selectedGoal === i ? C.primary : C.border}`,
                background: selectedGoal === i ? C.primaryLight : C.white,
              }}>
                <div style={{ fontSize: 13, fontWeight: selectedGoal === i ? 600 : 400, color: C.text, lineHeight: 1.5 }}>{g}</div>
              </div>
            ))}
            <div onClick={() => setGoalMode("custom")} style={{
              padding: "10px 16px", borderRadius: 8, cursor: "pointer",
              border: `1.5px dashed ${C.border}`, textAlign: "center",
              fontSize: 12, color: C.primary, fontWeight: 600,
            }}>✏️ Write my own goal instead</div>
          </div>
        )}

        {focus && goalMode === "custom" && (
          <div style={{ marginLeft: 30 }}>
            <textarea
              value={customGoalText}
              onChange={(e) => setCustomGoalText(e.target.value)}
              placeholder="e.g. I want to give specific, actionable feedback to my team after every sprint review"
              style={{ width: "100%", padding: "12px 14px", border: `1px solid ${C.border}`, borderRadius: 8, fontSize: 13, fontFamily: "inherit", resize: "vertical", minHeight: 70, color: C.text, boxSizing: "border-box" }}
            />
            <div style={{ fontSize: 11, color: customGoalText.length > 0 && customGoalText.length < 10 ? C.orange : C.textMuted, marginTop: 4, fontStyle: "italic" }}>
              {customGoalText.length > 0 && customGoalText.length < 10
                ? "⚠️ Too short — be specific about what you'll do differently."
                : "💡 Be specific: \"Give better feedback\" → \"Give specific positive feedback after at least 2 meetings per week.\""
              }
            </div>
            <div onClick={() => setGoalMode("suggestions")} style={{ fontSize: 12, color: C.primary, fontWeight: 600, cursor: "pointer", marginTop: 8 }}>← Back to suggestions</div>
          </div>
        )}
      </div>

      <div style={{ marginLeft: 30 }}>
        <Btn primary disabled={!focus || (goalMode === "suggestions" && selectedGoal === null) || (goalMode === "custom" && customGoalText.length < 10)} style={{ padding: "12px 32px", fontSize: 14 }}>Continue to self-assessment →</Btn>
        <div style={{ fontSize: 11, color: C.textMuted, marginTop: 8 }}>Next: quick baseline (under a minute), then your dashboard.</div>
      </div>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   SCREEN 1c: BASELINE ASSESSMENT
   ═══════════════════════════════════════════════ */
const BaselineAssessment = () => {
  const [ratings, setRatings] = useState({});
  const questions = [
    { id: "q1", text: "I give specific, actionable feedback regularly" },
    { id: "q2", text: "I feel confident giving constructive feedback" },
    { id: "q3", text: "My team members act on the feedback I provide" },
    { id: "q4", text: "I recognize team members' contributions publicly" },
    { id: "q5", text: "I address performance issues promptly" },
  ];
  const answered = Object.keys(ratings).length;

  return (
    <div style={{ padding: "32px 40px", maxWidth: 560 }}>
      <div style={{
        background: `linear-gradient(135deg, ${C.primary} 0%, ${C.primaryDark} 100%)`,
        borderRadius: 12, padding: "24px 28px", marginBottom: 24, color: "#fff", textAlign: "center",
      }}>
        <div style={{ fontSize: 32, marginBottom: 8 }}>📊</div>
        <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 4 }}>Baseline Self-Assessment</div>
        <div style={{ fontSize: 13, opacity: 0.8 }}>Rate yourself honestly — this is your starting point. Under a minute.</div>
      </div>

      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "20px 24px", marginBottom: 20 }}>
        <div style={{ fontSize: 14, fontWeight: 700, color: C.text, marginBottom: 4 }}>Rate yourself today</div>
        <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 6 }}>1 = Strongly disagree · 5 = Strongly agree</div>
        <div style={{ fontSize: 11, color: C.primary, marginBottom: 16 }}>Focus area: Giving feedback</div>
        {questions.map((q, i) => (
          <div key={q.id} style={{ padding: "14px 0", borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none" }}>
            <div style={{ fontSize: 13, color: C.text, marginBottom: 10 }}>{q.text}</div>
            <div style={{ display: "flex", gap: 6 }}>
              {[1, 2, 3, 4, 5].map(v => (
                <div key={v} onClick={() => setRatings(prev => ({ ...prev, [q.id]: v }))} style={{
                  width: 36, height: 36, borderRadius: 8, cursor: "pointer",
                  border: `2px solid ${ratings[q.id] === v ? C.primary : C.border}`,
                  background: ratings[q.id] === v ? C.primaryLight : C.white,
                  color: ratings[q.id] === v ? C.primary : C.textSec,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 14, fontWeight: 700,
                }}>{v}</div>
              ))}
            </div>
          </div>
        ))}
      </div>

      <Btn primary disabled={answered < 5} style={{ padding: "12px 28px" }}>
        {answered < 5 ? `Answer all questions (${answered}/5)` : "Continue to Dashboard →"}
      </Btn>
      <div style={{ fontSize: 11, color: C.textMuted, marginTop: 8 }}>
        🔒 {PRIVACY_TEXT}
      </div>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   SCREEN 2: DASHBOARD
   With Week 1 "Choose expert" state toggle
   ═══════════════════════════════════════════════ */
const Dashboard = ({ is180 }) => {
  const [editing, setEditing] = useState(false);
  const [isWeek1, setIsWeek1] = useState(false);

  return (
    <div style={{ padding: "24px 32px", maxWidth: 680 }}>
      {/* Demo toggle */}
      <div style={{ display: "flex", gap: 6, marginBottom: 12, padding: "6px 12px", background: C.primaryLight, borderRadius: 6, fontSize: 11, color: C.primaryDark }}>
        <strong>Demo:</strong>
        <span onClick={() => setIsWeek1(false)} style={{ padding: "2px 8px", borderRadius: 4, cursor: "pointer", background: !isWeek1 ? C.primary : "transparent", color: !isWeek1 ? "#fff" : C.primaryDark, fontWeight: 600 }}>Week 6 (normal)</span>
        <span onClick={() => setIsWeek1(true)} style={{ padding: "2px 8px", borderRadius: 4, cursor: "pointer", background: isWeek1 ? C.primary : "transparent", color: isWeek1 ? "#fff" : C.primaryDark, fontWeight: 600 }}>Week 1 (first time)</span>
      </div>

      <ProgramBanner is180={is180} />
      <GoalContext />

      {/* ── Week 1: Choose expert CTA (before first session booked) ── */}
      {isWeek1 && (
        <div style={{
          background: C.white, border: `1.5px solid ${C.primary}`, borderRadius: 12,
          padding: "16px 20px", marginBottom: 16,
          display: "flex", justifyContent: "space-between", alignItems: "center",
        }}>
          <div>
            <div style={{ fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 4 }}>Choose your expert & book first session</div>
            <div style={{ fontSize: 12, color: C.textSec }}>Browse curated experts matched to your program. This is your coaching partner for the next 90 days.</div>
          </div>
          <Btn primary small style={{ flexShrink: 0, marginLeft: 16 }}>Browse experts →</Btn>
        </div>
      )}

      {/* ── Practice card ── */}
      <div style={{
        background: C.white, border: `2px solid ${C.primary}`, borderRadius: 12,
        padding: "20px 24px", marginBottom: 16,
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: C.primary, textTransform: "uppercase", letterSpacing: 0.5 }}>This Week's Practice</div>
          <div style={{ fontSize: 11, color: C.textMuted }}>{isWeek1 ? "Week 1 · Mar 15–21" : "Week 6 · Apr 21–27"}</div>
        </div>
        {!editing ? (
          <>
            <div style={{ fontSize: 14, fontWeight: 600, color: C.text, lineHeight: 1.5, marginBottom: 8 }}>
              {isWeek1
                ? "This week, notice one moment where you could give feedback to a team member — just notice it, don't act yet."
                : "After your next team meeting, give specific positive feedback to one team member — name what they did well and why it mattered."
              }
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 6, flexWrap: "wrap" }}>
              <Tag>🤖 AI-generated</Tag>
              <Tag bg={C.greenLight} color={C.green}>Giving feedback</Tag>
              <span style={{ fontSize: 11, color: C.primary, cursor: "pointer", marginLeft: "auto", fontWeight: 600 }} onClick={() => setEditing(true)}>Change →</span>
            </div>
          </>
        ) : (
          <div>
            <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 8 }}>Edit or write your own:</div>
            <textarea
              defaultValue="After your next team meeting, give specific positive feedback to one team member — name what they did well and why it mattered."
              style={{ width: "100%", padding: "12px 14px", border: `1px solid ${C.primary}`, borderRadius: 8, fontSize: 13, fontFamily: "inherit", resize: "vertical", minHeight: 70, color: C.text, boxSizing: "border-box" }}
            />
            <div style={{ fontSize: 11, color: C.textMuted, marginTop: 4, marginBottom: 10 }}>💡 When? What exactly? How will you know you did it?</div>
            <div style={{ display: "flex", gap: 8 }}>
              <Btn primary small onClick={() => setEditing(false)}>✓ Save</Btn>
              <Btn ghost small onClick={() => setEditing(false)}>Cancel</Btn>
              <Btn ghost small>↻ New AI suggestion</Btn>
            </div>
          </div>
        )}
        {!isWeek1 && <WeekHistory />}
      </div>

      {/* ── Next Session ── */}
      {!isWeek1 && (
        <div style={{
          background: C.white, border: `1px solid ${C.border}`, borderRadius: 12,
          padding: "16px 20px",
          display: "flex", justifyContent: "space-between", alignItems: "center",
        }}>
          <div>
            <div style={{ fontSize: 12, fontWeight: 700, color: C.text, textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 6 }}>Next Session</div>
            <div style={{ fontSize: 14, fontWeight: 600, color: C.text }}>Wednesday, Apr 23 · 14:00</div>
            <div style={{ fontSize: 12, color: C.textSec, marginTop: 3 }}>Dana Brožková · Session 4 of 5</div>
          </div>
          <Btn primary small>Join</Btn>
        </div>
      )}

      <PrivacyLink />
    </div>
  );
};

/* ═══════════════════════════════════════════════
   SCREEN 3: FRIDAY CHECK-IN (inside practice card)
   + optional blocker reason for partial/no
   ═══════════════════════════════════════════════ */
const DashboardFriday = ({ is180 }) => {
  const [answered, setAnswered] = useState(null);
  const [showBlocker, setShowBlocker] = useState(false);

  return (
    <div style={{ padding: "24px 32px", maxWidth: 680 }}>
      <ProgramBanner is180={is180} />
      <GoalContext />

      <div style={{
        background: C.white,
        border: `2px solid ${answered ? (answered === "yes" ? C.green : answered === "partial" ? C.orange : C.border) : C.primary}`,
        borderRadius: 12, padding: "20px 24px", marginBottom: 16,
        opacity: (answered && !showBlocker) ? 0.7 : 1,
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: answered ? C.textMuted : C.primary, textTransform: "uppercase", letterSpacing: 0.5 }}>This Week's Practice</div>
          <div style={{ fontSize: 11, color: C.textMuted }}>Week 6 · Apr 21–27</div>
        </div>
        <div style={{ fontSize: 14, fontWeight: 600, color: C.text, lineHeight: 1.5, marginBottom: 8 }}>
          After your next team meeting, give specific positive feedback to one team member — name what they did well and why it mattered.
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 14 }}>
          <Tag>🤖 AI-generated</Tag>
          <Tag bg={C.greenLight} color={C.green}>Giving feedback</Tag>
        </div>

        {/* Check-in section */}
        <div style={{ borderTop: `1.5px solid ${answered ? (answered === "yes" ? C.green : C.orange) : C.orange}`, paddingTop: 14 }}>
          {!answered ? (
            <div>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: C.orange }}>How did it go this week?</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>Friday · Week 6</div>
              </div>
              <div style={{ display: "flex", gap: 8 }}>
                {[
                  { id: "yes", label: "✅ Yes, I did it" },
                  { id: "partial", label: "🔶 Partially" },
                  { id: "no", label: "❌ Not this week" },
                ].map(o => (
                  <div key={o.id} onClick={() => { setAnswered(o.id); if (o.id !== "yes") setShowBlocker(true); }} style={{
                    padding: "8px 14px", borderRadius: 8, cursor: "pointer",
                    border: `1.5px solid ${C.border}`, background: C.white,
                    fontSize: 12, fontWeight: 600, color: C.text,
                  }}>{o.label}</div>
                ))}
              </div>
            </div>
          ) : (
            <div>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: showBlocker ? 10 : 0 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: answered === "yes" ? C.green : answered === "partial" ? C.orange : C.textSec }}>
                  {answered === "yes" && "🎉 Great work! 5 weeks in a row. New practice on Monday."}
                  {answered === "partial" && "👍 Progress counts. New practice on Monday."}
                  {answered === "no" && "No worries — fresh start on Monday."}
                </div>
                <span style={{ fontSize: 11, color: C.textMuted }}>✓ Logged</span>
              </div>
              {/* Optional blocker reason for partial/no */}
              {showBlocker && answered !== "yes" && (
                <div style={{ marginTop: 4 }}>
                  <input
                    placeholder="What got in the way? (optional)"
                    style={{
                      width: "100%", padding: "8px 12px", border: `1px solid ${C.border}`, borderRadius: 6,
                      fontSize: 12, color: C.text, boxSizing: "border-box",
                    }}
                  />
                  <div style={{ display: "flex", gap: 8, marginTop: 6 }}>
                    <Btn ghost small onClick={() => setShowBlocker(false)}>Skip</Btn>
                    <Btn small outline onClick={() => setShowBlocker(false)}>Send</Btn>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
        <WeekHistory />
      </div>

      <div style={{
        background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "16px 20px",
        display: "flex", justifyContent: "space-between", alignItems: "center",
      }}>
        <div>
          <div style={{ fontSize: 12, fontWeight: 700, color: C.text, textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 6 }}>Next Session</div>
          <div style={{ fontSize: 14, fontWeight: 600, color: C.text }}>Wednesday, Apr 23 · 14:00</div>
          <div style={{ fontSize: 12, color: C.textSec, marginTop: 3 }}>Dana Brožková · Session 4 of 5</div>
        </div>
        <Btn primary small>Join</Btn>
      </div>
      <PrivacyLink />
    </div>
  );
};

/* ═══════════════════════════════════════════════
   SCREEN 4a: DASHBOARD + MID-CYCLE BANNER
   ═══════════════════════════════════════════════ */
const DashboardMidBanner = ({ is180, onStart }) => (
  <div style={{ padding: "24px 32px", maxWidth: 680 }}>
    <ProgramBanner is180={is180} />
    <GoalContext />
    <div style={{
      background: `linear-gradient(135deg, ${C.primaryLight} 0%, #E8EEFF 100%)`,
      border: `1.5px solid ${C.primary}`, borderRadius: 12,
      padding: "18px 22px", marginBottom: 16,
      display: "flex", justifyContent: "space-between", alignItems: "center",
    }}>
      <div>
        <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
          <span style={{ fontSize: 20 }}>🎯</span>
          <span style={{ fontSize: 14, fontWeight: 700, color: C.text }}>Mid-cycle check-in ready</span>
        </div>
        <div style={{ fontSize: 12, color: C.textSec }}>Same 5 questions as your baseline. See how far you've come. Under a minute.</div>
      </div>
      <Btn primary small onClick={onStart} style={{ flexShrink: 0, marginLeft: 16 }}>Start check-in →</Btn>
    </div>
    <div style={{
      background: C.white, border: `2px solid ${C.primary}`, borderRadius: 12,
      padding: "20px 24px", marginBottom: 16,
    }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
        <div style={{ fontSize: 12, fontWeight: 700, color: C.primary, textTransform: "uppercase", letterSpacing: 0.5 }}>This Week's Practice</div>
        <div style={{ fontSize: 11, color: C.textMuted }}>Week 7 · Apr 28 – May 4</div>
      </div>
      <div style={{ fontSize: 14, fontWeight: 600, color: C.text, lineHeight: 1.5, marginBottom: 8 }}>
        In your next 1:1, ask your team member what you could do differently as their lead — listen without defending.
      </div>
      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <Tag>🤖 AI-generated</Tag>
        <Tag bg={C.greenLight} color={C.green}>Giving feedback</Tag>
        <span style={{ fontSize: 11, color: C.primary, cursor: "pointer", marginLeft: "auto", fontWeight: 600 }}>Change →</span>
      </div>
      <WeekHistory />
    </div>
    <div style={{
      background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "16px 20px",
      display: "flex", justifyContent: "space-between", alignItems: "center",
    }}>
      <div>
        <div style={{ fontSize: 12, fontWeight: 700, color: C.text, textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 6 }}>Next Session</div>
        <div style={{ fontSize: 14, fontWeight: 600, color: C.text }}>Wednesday, May 7 · 14:00</div>
        <div style={{ fontSize: 12, color: C.textSec, marginTop: 3 }}>Dana Brožková · Session 5 of 5</div>
      </div>
      <Btn primary small>Join</Btn>
    </div>
  </div>
);

/* ═══════════════════════════════════════════════
   SCREEN 4b: MID-CYCLE QUESTIONS
   The assessment form. After submit → navigates to 4c.
   ═══════════════════════════════════════════════ */
const MidQuestions = ({ onSubmit }) => {
  const [ratings, setRatings] = useState({});
  const questions = [
    { id: "q1", text: "I give specific, actionable feedback regularly" },
    { id: "q2", text: "I feel confident giving constructive feedback" },
    { id: "q3", text: "My team members act on the feedback I provide" },
    { id: "q4", text: "I recognize team members' contributions publicly" },
    { id: "q5", text: "I address performance issues promptly" },
  ];
  const answered = Object.keys(ratings).length;

  return (
    <div style={{ padding: "32px 40px", maxWidth: 560 }}>
      <div style={{
        background: `linear-gradient(135deg, ${C.primary} 0%, #8B7CF0 100%)`,
        borderRadius: 12, padding: "24px 28px", marginBottom: 24, color: "#fff", textAlign: "center",
      }}>
        <div style={{ fontSize: 32, marginBottom: 8 }}>🎯</div>
        <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 4 }}>Mid-cycle check-in</div>
        <div style={{ fontSize: 13, opacity: 0.8 }}>Same 5 questions. Under a minute.</div>
      </div>
      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "20px 24px", marginBottom: 20 }}>
        <div style={{ fontSize: 14, fontWeight: 700, color: C.text, marginBottom: 4 }}>Rate yourself today</div>
        <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 20 }}>1 = Strongly disagree · 5 = Strongly agree. Answer honestly — you'll see your progress after submitting.</div>
        {questions.map((q, i) => (
          <div key={q.id} style={{ padding: "14px 0", borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none" }}>
            <div style={{ fontSize: 13, color: C.text, marginBottom: 10 }}>{q.text}</div>
            <div style={{ display: "flex", gap: 6 }}>
              {[1, 2, 3, 4, 5].map(v => (
                <div key={v} onClick={() => setRatings(prev => ({ ...prev, [q.id]: v }))} style={{
                  width: 36, height: 36, borderRadius: 8, cursor: "pointer",
                  border: `2px solid ${ratings[q.id] === v ? C.primary : C.border}`,
                  background: ratings[q.id] === v ? C.primaryLight : C.white,
                  color: ratings[q.id] === v ? C.primary : C.textSec,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 14, fontWeight: 700,
                }}>{v}</div>
              ))}
              {/* NO inline delta — shown only on result screen after submit */}
            </div>
          </div>
        ))}
      </div>
      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "16px 20px", marginBottom: 20 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 8 }}>What's been most useful so far?</div>
        <textarea placeholder="e.g. The weekly practices helped me notice opportunities..." style={{
          width: "100%", padding: "10px 12px", border: `1px solid ${C.border}`, borderRadius: 8,
          fontSize: 13, fontFamily: "inherit", resize: "vertical", minHeight: 60, color: C.text, boxSizing: "border-box",
        }} />
      </div>
      <Btn primary disabled={answered < 5} onClick={answered >= 5 ? onSubmit : undefined} style={{ padding: "12px 28px" }}>
        {answered < 5 ? `Answer all questions (${answered}/5)` : "Submit Check-in"}
      </Btn>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   SCREEN 4c: MID-CYCLE RESULT
   Shown after submit. Start → Now (2 columns only).
   Flow: 4a (banner) → 4b (questions) → 4c (result) → Dashboard
   ═══════════════════════════════════════════════ */
const MidResult = () => {
  const results = [
    { label: "Giving specific feedback", base: 2, mid: 3 },
    { label: "Confidence in feedback", base: 3, mid: 4 },
    { label: "Team acts on feedback", base: 2, mid: 3 },
    { label: "Public recognition", base: 3, mid: 3 },
    { label: "Addressing issues", base: 4, mid: 3 },
  ];

  return (
    <div style={{ padding: "32px 40px", maxWidth: 560 }}>
      <div style={{
        background: `linear-gradient(135deg, ${C.primary} 0%, #8B7CF0 100%)`,
        borderRadius: 12, padding: "24px 28px", marginBottom: 24, color: "#fff", textAlign: "center",
      }}>
        <div style={{ fontSize: 32, marginBottom: 8 }}>📊</div>
        <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 4 }}>Your progress so far</div>
        <div style={{ fontSize: 13, opacity: 0.8 }}>Halfway through — here's how you're tracking.</div>
      </div>

      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "20px 24px", marginBottom: 20 }}>
        {/* Header — Start and Now only */}
        <div style={{ display: "flex", alignItems: "center", padding: "0 0 8px", borderBottom: `1px solid ${C.borderLight}`, marginBottom: 4 }}>
          <div style={{ flex: 1, fontSize: 10, fontWeight: 600, color: C.textMuted, textTransform: "uppercase", letterSpacing: 0.3 }}>Question</div>
          <div style={{ display: "flex", alignItems: "center" }}>
            <div style={{ width: 36, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>Start</div>
            <div style={{ width: 20 }} />
            <div style={{ width: 36, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>Now</div>
            <div style={{ width: 52, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>Change</div>
          </div>
        </div>

        {results.map((item, i) => {
          const delta = item.mid - item.base;
          const dir = delta > 0 ? "up" : delta < 0 ? "down" : "same";
          return (
            <div key={i} style={{
              display: "flex", alignItems: "center", padding: "10px 0",
              borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none",
            }}>
              <div style={{ flex: 1, fontSize: 12, color: C.text, fontWeight: 500 }}>{item.label}</div>
              <div style={{ display: "flex", alignItems: "center" }}>
                <div style={{ width: 36, textAlign: "center", fontSize: 15, fontWeight: 700, color: C.textMuted }}>{item.base}</div>
                <div style={{ width: 20, textAlign: "center", fontSize: 12, fontWeight: 700, color: dir === "up" ? C.green : dir === "down" ? C.orange : C.textMuted }}>→</div>
                <div style={{ width: 36, textAlign: "center", fontSize: 15, fontWeight: 700, color: dir === "up" ? C.green : dir === "down" ? C.orange : C.textMuted }}>{item.mid}</div>
                <div style={{ width: 52, textAlign: "center", fontSize: 13, fontWeight: 700, color: dir === "up" ? C.green : dir === "down" ? C.orange : C.textMuted }}>
                  {delta > 0 ? `+${delta} ↑` : delta < 0 ? `${delta} ↓` : "–"}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{
        background: C.primaryLight, borderRadius: 8, padding: "12px 16px", marginBottom: 20,
        fontSize: 12, color: C.primaryDark, lineHeight: 1.6,
      }}>
        💡 You're halfway through. The final assessment at the end of the cycle will show your full journey: Start → Mid → End.
      </div>

      <Btn primary style={{ padding: "12px 28px" }}>Back to Dashboard →</Btn>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   SCREEN 5: CYCLE/FINAL REVIEW
   Celebration with Baseline → Mid → End
   Reordered CTAs: View progress first, share second
   ═══════════════════════════════════════════════ */
const CycleReview = ({ is180 }) => {
  const [view, setView] = useState("assessment"); // FIXED: starts with assessment, not celebration
  const [showMore2, setShowMore2] = useState(false);
  const [cycle2Focus, setCycle2Focus] = useState("Giving feedback");
  const [ratings, setRatings] = useState({});
  const [nps, setNps] = useState(null);
  const isFinal = !is180;

  const questions = [
    { id: "q1", text: "I give specific, actionable feedback regularly" },
    { id: "q2", text: "I feel confident giving constructive feedback" },
    { id: "q3", text: "My team members act on the feedback I provide" },
    { id: "q4", text: "I recognize team members' contributions publicly" },
    { id: "q5", text: "I address performance issues promptly" },
  ];
  const answered = Object.keys(ratings).length;
  const canSubmit = answered >= 5 && (!isFinal || nps !== null);

  // FIRST: Assessment (participant fills in before seeing results)
  if (view === "assessment") {
    return (
      <div style={{ padding: "32px 40px", maxWidth: 560 }}>
        <div style={{
          background: `linear-gradient(135deg, ${C.green} 0%, #00D2A0 100%)`,
          borderRadius: 12, padding: "24px 28px", marginBottom: 24, color: "#fff", textAlign: "center",
        }}>
          <div style={{ fontSize: 32, marginBottom: 8 }}>{isFinal ? "🏁" : "🔄"}</div>
          <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 4 }}>{isFinal ? "Final Review" : "Cycle 1 Review"}</div>
          <div style={{ fontSize: 13, opacity: 0.85 }}>Last assessment{isFinal ? " + NPS" : ""}. Under a minute.</div>
        </div>

        {/* 5 Likert questions — NO inline delta */}
        <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "20px 24px", marginBottom: 16 }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: C.text, marginBottom: 4 }}>Self-assessment</div>
          <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 20 }}>1 = Strongly disagree · 5 = Strongly agree. No delta shown — answer honestly.</div>
          {questions.map((q, i) => (
            <div key={q.id} style={{ padding: "14px 0", borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none" }}>
              <div style={{ fontSize: 13, color: C.text, marginBottom: 10 }}>{q.text}</div>
              <div style={{ display: "flex", gap: 6 }}>
                {[1, 2, 3, 4, 5].map(v => (
                  <div key={v} onClick={() => setRatings(prev => ({ ...prev, [q.id]: v }))} style={{
                    width: 36, height: 36, borderRadius: 8, cursor: "pointer",
                    border: `2px solid ${ratings[q.id] === v ? C.primary : C.border}`,
                    background: ratings[q.id] === v ? C.primaryLight : C.white,
                    color: ratings[q.id] === v ? C.primary : C.textSec,
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 14, fontWeight: 700,
                  }}>{v}</div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Open text */}
        <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "16px 20px", marginBottom: 16 }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 8 }}>What's the biggest change you've noticed?</div>
          <textarea placeholder="Write here... (optional)" style={{ width: "100%", padding: "10px 12px", border: `1px solid ${C.border}`, borderRadius: 8, fontSize: 13, fontFamily: "inherit", resize: "vertical", minHeight: 60, color: C.text, boxSizing: "border-box" }} />
        </div>

        {/* NPS — final cycle only */}
        {isFinal && (
          <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, padding: "16px 20px", marginBottom: 16 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 10 }}>How likely would you recommend this program to a colleague?</div>
            <div style={{ display: "flex", gap: 4 }}>
              {[0,1,2,3,4,5,6,7,8,9,10].map(v => (
                <div key={v} onClick={() => setNps(v)} style={{
                  flex: 1, height: 36, borderRadius: 6, cursor: "pointer",
                  border: `2px solid ${nps === v ? C.primary : C.border}`,
                  background: nps === v ? C.primaryLight : C.white,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 12, fontWeight: 700, color: nps === v ? C.primary : C.textSec,
                }}>{v}</div>
              ))}
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", marginTop: 4, fontSize: 10, color: C.textMuted }}><span>Not at all likely</span><span>Extremely likely</span></div>
          </div>
        )}

        <Btn primary disabled={!canSubmit} onClick={canSubmit ? () => setView("celebration") : undefined} style={{ padding: "12px 28px" }}>
          {!canSubmit
            ? isFinal
              ? `Answer all questions + NPS (${answered}/5${nps !== null ? " + NPS ✓" : ""})`
              : `Answer all questions (${answered}/5)`
            : isFinal ? "Complete Program →" : "Complete Cycle 1 →"
          }
        </Btn>
      </div>
    );
  }

  // SECOND: Celebration (shown AFTER assessment submit)
  return (
    <div style={{ padding: "32px 40px", maxWidth: 560 }}>
      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 16, padding: "40px 32px", textAlign: "center" }}>
        <div style={{ fontSize: 56, marginBottom: 12 }}>{isFinal ? "🎉" : "🔄"}</div>
        <div style={{ fontSize: 22, fontWeight: 700, color: C.text, marginBottom: 4 }}>{isFinal ? "Program Completed!" : "Cycle 1 Complete!"}</div>
        <div style={{ fontSize: 14, color: C.textSec, marginBottom: 4 }}>Leadership Development {is180 ? "2026" : "Q1 2026"}</div>
        {!isFinal && <div style={{ fontSize: 12, color: C.primary, fontWeight: 600, marginBottom: 20 }}>Cycle 2 starts next week — adjust your focus below.</div>}

        {/* Journey: clean header + number rows */}
        <div style={{ background: C.bg, borderRadius: 12, padding: "20px 24px", marginBottom: 24, textAlign: "left" }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: C.text, textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 12 }}>
            {isFinal ? "Your Journey" : "Cycle 1 Progress"}
          </div>

          {/* Header row */}
          <div style={{ display: "flex", alignItems: "center", padding: "0 0 8px", borderBottom: `1px solid ${C.borderLight}`, marginBottom: 4 }}>
            <div style={{ flex: 1, fontSize: 10, fontWeight: 600, color: C.textMuted, textTransform: "uppercase", letterSpacing: 0.3 }}>Question</div>
            <div style={{ display: "flex", alignItems: "center", gap: 0 }}>
              <div style={{ width: 36, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>Start</div>
              <div style={{ width: 20 }} />
              <div style={{ width: 36, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>Mid</div>
              <div style={{ width: 20 }} />
              <div style={{ width: 36, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>End</div>
              <div style={{ width: 52, textAlign: "center", fontSize: 10, fontWeight: 600, color: C.textMuted }}>Change</div>
            </div>
          </div>

          {[
            { label: "Giving specific feedback", base: 2, mid: 3, end: 4 },
            { label: "Confidence in feedback", base: 3, mid: 4, end: 5 },
            { label: "Team acts on feedback", base: 2, mid: 4, end: 3 },
            { label: "Public recognition", base: 3, mid: 3, end: 3 },
            { label: "Addressing issues", base: 4, mid: 3, end: 2 },
          ].map((item, i) => {
            const delta = item.end - item.base;
            const midDir = item.mid > item.base ? "up" : item.mid < item.base ? "down" : "same";
            const endDir = item.end > item.mid ? "up" : item.end < item.mid ? "down" : "same";
            const arrowColor = (dir) => dir === "up" ? C.green : dir === "down" ? C.orange : C.textMuted;

            return (
              <div key={i} style={{
                display: "flex", alignItems: "center",
                padding: "10px 0",
                borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none",
              }}>
                <div style={{ flex: 1, fontSize: 12, color: C.text, fontWeight: 500 }}>{item.label}</div>
                <div style={{ display: "flex", alignItems: "center", gap: 0 }}>
                  {/* Start */}
                  <div style={{ width: 36, textAlign: "center", fontSize: 15, fontWeight: 700, color: C.textMuted }}>{item.base}</div>
                  {/* Arrow 1 */}
                  <div style={{ width: 20, textAlign: "center", fontSize: 12, fontWeight: 700, color: arrowColor(midDir) }}>→</div>
                  {/* Mid */}
                  <div style={{ width: 36, textAlign: "center", fontSize: 15, fontWeight: 700, color: C.textSec }}>{item.mid}</div>
                  {/* Arrow 2 */}
                  <div style={{ width: 20, textAlign: "center", fontSize: 12, fontWeight: 700, color: arrowColor(endDir) }}>→</div>
                  {/* End */}
                  <div style={{ width: 36, textAlign: "center", fontSize: 15, fontWeight: 700, color: delta > 0 ? C.green : delta < 0 ? C.orange : C.textMuted }}>{item.end}</div>
                  {/* Delta */}
                  <div style={{ width: 52, textAlign: "center", fontSize: 13, fontWeight: 700, color: delta > 0 ? C.green : delta < 0 ? C.orange : C.textMuted }}>
                    {delta > 0 ? `+${delta} ↑` : delta < 0 ? `${delta} ↓` : "–"}
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        <div style={{ display: "flex", gap: 12, marginBottom: 24 }}>
          {[
            { value: isFinal ? "5/5" : "3/3", label: "Sessions", icon: "🎯" },
            { value: "11/13", label: "Practices", icon: "✅" },
            { value: "+1.0", label: "Avg. improvement", icon: "📈" },
          ].map((s, i) => (
            <div key={i} style={{ flex: 1, background: C.bg, borderRadius: 10, padding: "14px 12px", textAlign: "center" }}>
              <div style={{ fontSize: 18, marginBottom: 4 }}>{s.icon}</div>
              <div style={{ fontSize: 18, fontWeight: 700, color: C.text }}>{s.value}</div>
              <div style={{ fontSize: 11, color: C.textMuted }}>{s.label}</div>
            </div>
          ))}
        </div>

        {isFinal ? (
          <>
            <div style={{
              display: "inline-flex", alignItems: "center", gap: 8,
              background: `linear-gradient(135deg, ${C.primaryLight} 0%, #E8F0FF 100%)`,
              border: `1.5px solid ${C.primary}`, borderRadius: 10, padding: "10px 18px", marginBottom: 24,
            }}>
              <span style={{ fontSize: 22 }}>🏆</span>
              <div style={{ textAlign: "left" }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: C.primary }}>Leadership Program Graduate</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>Completed Mar – Jun 2026</div>
              </div>
            </div>
            {/* CTAs — assessment already completed, this is the reward */}
            <div style={{ display: "flex", gap: 10, justifyContent: "center" }}>
              <Btn primary>Download summary</Btn>
              <Btn outline>Share with manager</Btn>
            </div>
            <div style={{ fontSize: 11, color: C.textMuted, marginTop: 10 }}>Sharing is optional and voluntary. Manager sees summary only, never session content.</div>
          </>
        ) : (
          <>
            <div style={{
              background: C.white, border: `1.5px solid ${C.primary}`, borderRadius: 12,
              padding: "16px 20px", marginBottom: 16, textAlign: "left",
            }}>
              <div style={{ fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 4 }}>Set focus for Cycle 2</div>
              <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 12 }}>Keep the same area or shift to something new. Choose carefully — stays fixed for Cycle 2.</div>

              {/* Recommended (from program) */}
              <div style={{ marginBottom: 8 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: C.primary, marginBottom: 6 }}>Program areas</div>
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                  {["Giving feedback", "Delegation"].map(a => (
                    <Chip key={a} selected={cycle2Focus === a} onClick={() => setCycle2Focus(a)}>{a}</Chip>
                  ))}
                </div>
              </div>

              {/* Expandable: full library */}
              {!showMore2 ? (
                <div onClick={() => setShowMore2(true)} style={{ fontSize: 12, color: C.primary, fontWeight: 600, cursor: "pointer", padding: "6px 0" }}>
                  + Show more areas (8 available) →
                </div>
              ) : (
                <div>
                  <div style={{ fontSize: 11, fontWeight: 600, color: C.textMuted, marginBottom: 6, marginTop: 4 }}>All available areas</div>
                  <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    {["Self-awareness", "Strategic thinking", "Team motivation", "Communication", "Conflict resolution", "Time management", "Emotional intelligence", "Decision making"].map(a => (
                      <Chip key={a} selected={cycle2Focus === a} onClick={() => setCycle2Focus(a)}>{a}</Chip>
                    ))}
                  </div>
                  <div onClick={() => setShowMore2(false)} style={{ fontSize: 11, color: C.textMuted, cursor: "pointer", marginTop: 6 }}>← Show less</div>
                </div>
              )}

              <div style={{ fontSize: 12, color: C.textMuted, marginTop: 12, paddingTop: 10, borderTop: `1px solid ${C.borderLight}` }}>
                Cycle 2 baseline = your current end scores. New assessment questions will match your selected area.
              </div>
            </div>
            <Btn primary>Start Cycle 2 →</Btn>
          </>
        )}
      </div>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   MAIN APP
   ═══════════════════════════════════════════════ */
const screens = [
  { id: "welcome", label: "1a. Welcome" },
  { id: "enrollment", label: "1b. Enrollment" },
  { id: "baseline", label: "1c. Baseline" },
  { id: "dashboard", label: "2. Dashboard" },
  { id: "friday", label: "3. Friday" },
  { id: "midbanner", label: "4a. Mid Banner" },
  { id: "midcheck", label: "4b. Mid Questions" },
  { id: "midresult", label: "4c. Mid Result" },
  { id: "review", label: "5. Review → Celebration" },
];

export default function ParticipantExperience() {
  const [screen, setScreen] = useState("welcome");
  const [is180, setIs180] = useState(false);

  return (
    <div style={{ fontFamily: "-apple-system, 'Segoe UI', sans-serif", background: C.bg, minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <div style={{ background: "#1A1A2E", color: "#fff", padding: "10px 20px", display: "flex", alignItems: "center", gap: 5, fontSize: 12, flexWrap: "wrap" }}>
        <span style={{ fontWeight: 700, opacity: 0.5, textTransform: "uppercase", letterSpacing: 1, marginRight: 6 }}>Screen:</span>
        {screens.map(s => (
          <button key={s.id} onClick={() => setScreen(s.id)} style={{
            background: screen === s.id ? C.primary : "rgba(255,255,255,0.1)",
            color: "#fff", border: "none", borderRadius: 6,
            padding: "4px 8px", fontSize: 10, fontWeight: 600, cursor: "pointer",
          }}>{s.label}</button>
        ))}
        <span style={{ margin: "0 6px", opacity: 0.3 }}>|</span>
        <button onClick={() => setIs180(false)} style={{ background: !is180 ? C.green : "rgba(255,255,255,0.1)", color: "#fff", border: "none", borderRadius: 6, padding: "4px 8px", fontSize: 10, fontWeight: 600, cursor: "pointer" }}>90d</button>
        <button onClick={() => setIs180(true)} style={{ background: is180 ? C.green : "rgba(255,255,255,0.1)", color: "#fff", border: "none", borderRadius: 6, padding: "4px 8px", fontSize: 10, fontWeight: 600, cursor: "pointer" }}>180d</button>
      </div>

      <div style={{ display: "flex", flex: 1, overflow: "hidden" }}>
        <Sidebar />
        <div style={{ flex: 1, overflow: "auto" }}>
          <div style={{ padding: "12px 32px", borderBottom: `1px solid ${C.border}`, background: C.white, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <div style={{ fontSize: 13, color: C.textSec }}>
              {screen === "welcome" && "Welcome to TopLeader!"}
              {screen === "enrollment" && "Let's set up your program."}
              {screen === "baseline" && "Quick self-assessment."}
              {screen === "dashboard" && "Hello, Tomáš!"}
              {screen === "friday" && "Friday reflection"}
              {screen === "midbanner" && "Mid-cycle check-in ready"}
              {screen === "midcheck" && "Mid-cycle Questions"}
              {screen === "midresult" && "Your Progress So Far"}
              {screen === "review" && (is180 ? "Cycle 1 Review" : "Program Completion")}
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <div style={{ width: 30, height: 30, borderRadius: 15, background: C.primaryLight, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 13, fontWeight: 700, color: C.primary }}>T</div>
              <span style={{ fontSize: 13, fontWeight: 600, color: C.text }}>Tomáš Novák</span>
            </div>
          </div>

          {screen === "welcome" && <Welcome onContinue={() => setScreen("enrollment")} />}
          {screen === "enrollment" && <Enrollment />}
          {screen === "baseline" && <BaselineAssessment />}
          {screen === "dashboard" && <Dashboard is180={is180} />}
          {screen === "friday" && <DashboardFriday is180={is180} />}
          {screen === "midbanner" && <DashboardMidBanner is180={is180} onStart={() => setScreen("midcheck")} />}
          {screen === "midcheck" && <MidQuestions onSubmit={() => setScreen("midresult")} />}
          {screen === "midresult" && <MidResult />}
          {screen === "review" && <CycleReview is180={is180} />}
        </div>
      </div>

      <div style={{ background: "#F8F7FF", borderTop: `1px solid ${C.border}`, padding: "12px 24px" }}>
        <div style={{ fontSize: 11, color: C.primaryDark, lineHeight: 1.6 }}>
          <strong>Participant Flow:</strong>{" "}
          {screen === "welcome" && "1a. WELCOME: Nový screen před enrollmentem. Participant chápe PROČ je tu, CO ho čeká (5 sessions, weekly practice, 3 check-iny), KOLIK času to zabere, a CO HR vidí/nevidí. CTA 'Let's get started' → enrollment. Bez tohoto kontextu je enrollment příliš náhlý."}
          {screen === "enrollment" && "1b. ENROLLMENT: Focus area výběr ze DVOU VRSTEV: nahoře 'Recommended for this program' (HR-definované, 2–3 chipy), pod nimi '+ Show more areas' odkaz který rozbalí zbylých 8 z knihovny. ŽÁDNÝ free-text 'Other' — všechny oblasti mají assessment otázky, takže měření funguje vždy. Po výběru oblasti → AI-generované návrhy cílů (3 klikací) + 'Write my own' pro custom formulaci. Fallback goals pro oblasti bez specifických návrhů."}
          {screen === "baseline" && "1c. BASELINE: 5 Likert, žádný delta. Povinné — Dashboard blokovaný. CTA disabled dokud není 5/5. Privacy note na spodu konzistentní s Welcome screenem."}
          {screen === "dashboard" && "2. DASHBOARD: Přepni demo toggle — Week 1 ukazuje 'Choose your expert' CTA (odkaz na Coaches, filtrované dle shortlistu). Week 6 ukazuje normální stav. Auto-active practice, Change → link, WeekHistory. 🔒 PrivacyLink persistentní na každé stránce."}
          {screen === "friday" && "3. FRIDAY: Check-in uvnitř practice karty. Po kliknutí Partially/No se objeví optional input 'What got in the way?' + Skip/Send. Jednoduché zachycení blockeru — cenné pro kouče i AI. PrivacyLink na spodu."}
          {screen === "midbanner" && "4a. MID BANNER: Banner nad practice kartou. Neblokuje. CTA naviguje na assessment stránku (4b)."}
          {screen === "midcheck" && "4b. MID QUESTIONS: 5 Likert BEZ inline delta — participant nevidí baseline porovnání během odpovídání (anchoring bias prevention). Delta se ukáže AŽ na result screenu (4c). Gate: submit disabled dokud není 5/5 (stejný pattern jako baseline). Hint: 'Answer honestly — you'll see your progress after submitting.'"}
          {screen === "midresult" && "4c. MID RESULT: Start → Now (2 sloupce). Tady participant POPRVÉ vidí svůj delta — po poctivém zodpovězení. Silnější emoční moment než inline delta. Flow: 4a → 4b (otázky bez delta) → 4c (výsledek s delta) → dashboard."}
          {screen === "review" && `5. REVIEW: OPRAVENÉ POŘADÍ — assessment PRVNÍ, celebration DRUHÁ. Participant nejdřív vyplní 5 Likert (bez inline delta, stejně jako mid) + open text + NPS (jen final). Klik 'Complete' → celebration s journey summary (Start→Mid→End). Celebration CTAs: 'Download summary' primary, 'Share with manager' secondary. ${is180 ? "180d: Po celebration → Cycle 2 focus area selector." : "90d: Final celebration s badge."}`}
        </div>
      </div>
    </div>
  );
}
