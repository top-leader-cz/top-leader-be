import { useState } from "react";

const C = {
  primary: "#6C5CE7", primaryDark: "#5A4BD1", primaryLight: "#F0EEFF",
  bg: "#FAFAFE", white: "#FFFFFF", text: "#1A1A2E", textSec: "#6B7089",
  textMuted: "#9B9DB7", border: "#E8E8F0", borderLight: "#F0F0F6",
  green: "#00B894", greenLight: "#E8FBF5", orange: "#F39C12", orangeLight: "#FEF6E7",
  red: "#E74C3C", redLight: "#FDE8E8",
};

/* ── Shared UI ───────────────────────────────── */
const Btn = ({ children, primary, outline, disabled, small, onClick, style }) => (
  <button onClick={disabled ? undefined : onClick} style={{
    background: disabled ? "#D0D0DD" : primary ? C.primary : "transparent",
    color: disabled ? "#fff" : primary ? "#fff" : outline ? C.primary : C.textSec,
    border: outline ? `1.5px solid ${C.primary}` : primary ? "none" : `1px solid ${C.border}`,
    borderRadius: 8, padding: small ? "6px 14px" : "10px 22px",
    fontSize: 13, fontWeight: 600, cursor: disabled ? "not-allowed" : "pointer",
    display: "inline-flex", alignItems: "center", gap: 6, ...style,
  }}>{children}</button>
);

const Input = ({ label, placeholder, value, hint, textarea, readOnly }) => (
  <div style={{ marginBottom: 16 }}>
    <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 6 }}>{label}</label>
    {textarea ? (
      <textarea readOnly={readOnly} placeholder={placeholder} defaultValue={value} style={{
        width: "100%", padding: "10px 14px", border: `1px solid ${C.border}`, borderRadius: 8,
        fontSize: 13, fontFamily: "inherit", resize: "vertical", minHeight: 60,
        background: readOnly ? C.borderLight : C.white, color: C.text, boxSizing: "border-box",
      }} />
    ) : (
      <input readOnly={readOnly} placeholder={placeholder} defaultValue={value} style={{
        width: "100%", padding: "10px 14px", border: `1px solid ${C.border}`, borderRadius: 8,
        fontSize: 13, background: readOnly ? C.borderLight : C.white, color: C.text, boxSizing: "border-box",
      }} />
    )}
    {hint && <div style={{ fontSize: 11, color: C.textMuted, marginTop: 4 }}>{hint}</div>}
  </div>
);

const Chip = ({ children, selected, onClick }) => (
  <span onClick={onClick} style={{
    display: "inline-block", padding: "6px 14px", borderRadius: 20, fontSize: 12.5, fontWeight: 500,
    background: selected ? C.primaryLight : C.white, color: selected ? C.primary : C.textSec,
    border: `1.5px solid ${selected ? C.primary : C.border}`,
    cursor: "pointer", transition: "all 0.15s", userSelect: "none",
  }}>{selected ? "✓ " : ""}{children}</span>
);

const Card = ({ children, selected, onClick, style }) => (
  <div onClick={onClick} style={{
    background: C.white, border: `1.5px solid ${selected ? C.primary : C.border}`,
    borderRadius: 10, padding: 16, cursor: onClick ? "pointer" : "default",
    transition: "all 0.15s", boxShadow: selected ? `0 0 0 3px ${C.primaryLight}` : "none", ...style,
  }}>{children}</div>
);

const Note = ({ children, type = "info" }) => (
  <div style={{
    padding: "10px 14px", borderRadius: 8, fontSize: 12, lineHeight: 1.5,
    background: type === "info" ? C.primaryLight : C.orangeLight,
    color: type === "info" ? C.primaryDark : C.orange, marginBottom: 16, marginTop: 8,
  }}>{children}</div>
);

/* ═══════════════════════════════════════════════
   STEP 1: PROGRAM SETUP
   ═══════════════════════════════════════════════ */
const Step1 = () => {
  const [template, setTemplate] = useState("leadership");
  const [focusAreas, setFocusAreas] = useState(["Giving feedback", "Delegation"]);
  const allAreas = ["Giving feedback", "Delegation", "Self-awareness", "Strategic thinking", "Team motivation", "Communication", "Conflict resolution", "Time management", "Emotional intelligence", "Decision making"];
  const toggleArea = (a) => setFocusAreas(prev => prev.includes(a) ? prev.filter(x => x !== a) : [...prev, a]);

  return (
    <div>
      <div style={{ fontSize: 18, fontWeight: 700, color: C.text, marginBottom: 4 }}>Program Setup</div>
      <div style={{ fontSize: 13, color: C.textSec, marginBottom: 24 }}>Define what this program is about and who it's for.</div>

      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 10 }}>Start from template</label>
      <div style={{ display: "flex", gap: 10, marginBottom: 24 }}>
        {[
          { id: "leadership", icon: "👑", name: "Leadership 90d", desc: "Feedback, delegation, accountability" },
          { id: "stress", icon: "🧘", name: "Stress Resilience 90d", desc: "Burnout prevention, coping" },
          { id: "newmgr", icon: "🚀", name: "New Manager 90d", desc: "First 90 days in role" },
          { id: "custom", icon: "✏️", name: "Custom", desc: "Build from scratch" },
        ].map(t => (
          <Card key={t.id} selected={template === t.id} onClick={() => setTemplate(t.id)} style={{ flex: 1, textAlign: "center" }}>
            <div style={{ fontSize: 24, marginBottom: 6 }}>{t.icon}</div>
            <div style={{ fontSize: 13, fontWeight: 600, color: C.text }}>{t.name}</div>
            <div style={{ fontSize: 11, color: C.textMuted, marginTop: 3 }}>{t.desc}</div>
          </Card>
        ))}
      </div>

      <Input label="Program name" placeholder="e.g. Leadership Development Q1 2026" value="Leadership Development Q1 2026" hint="Internal name visible to HR and participants" />
      <Input label="Program goal" placeholder="e.g. Improve feedback culture & accountability for Team Leads" value="Improve feedback culture & accountability for Team Leads" textarea hint="One sentence. Visible to participants as the program's purpose. Replaces individual long-term goals." />
      <Input label="Target group (optional)" placeholder="e.g. Team Leads, Engineering division" value="Team Leads, Engineering division" hint="Who is this program for?" />

      <div style={{ marginBottom: 16 }}>
        <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 4 }}>Focus areas</label>
        <div style={{ fontSize: 11, color: C.textMuted, marginBottom: 10 }}>Select 2–5 areas. Participant will choose 1 as their primary focus for the cycle.</div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginBottom: 10 }}>
          {allAreas.map(a => <Chip key={a} selected={focusAreas.includes(a)} onClick={() => toggleArea(a)}>{a}</Chip>)}
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <input placeholder="+ Add custom focus area" style={{ padding: "7px 12px", border: `1px solid ${C.border}`, borderRadius: 8, fontSize: 12, width: 220, color: C.text }} />
          <Btn small outline>Add</Btn>
        </div>
      </div>

      <Note>ℹ️ Focus areas define the "menu" participants choose from during enrollment. They can also write their own.</Note>

      <div style={{ display: "flex", gap: 16 }}>
        <div style={{ flex: 1 }}><Input label="Duration" value="90 days" readOnly hint="Based on template. Custom can adjust." /></div>
        <div style={{ flex: 1 }}><Input label="Start date" placeholder="Select date" value="2026-03-15" hint="When participants get enrolled" /></div>
      </div>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   STEP 2: PARTICIPANTS
   ═══════════════════════════════════════════════ */
const Step2 = () => {
  const [method, setMethod] = useState("select");
  const people = [
    { name: "Tomáš Novák", email: "tomas.n@company.cz", added: true },
    { name: "Jana Dvořáková", email: "jana.d@company.cz", added: true },
    { name: "Petr Svoboda", email: "petr.s@company.cz", added: true },
    { name: "Markéta Černá", email: "marketa.c@company.cz", added: true },
    { name: "Martin Procházka", email: "martin.p@company.cz", added: false, conflict: null },
    { name: "Lucie Kolářová", email: "lucie.k@company.cz", added: false, conflict: "Stress Resilience Q1" },
  ];

  return (
    <div>
      <div style={{ fontSize: 18, fontWeight: 700, color: C.text, marginBottom: 4 }}>Participants</div>
      <div style={{ fontSize: 13, color: C.textSec, marginBottom: 24 }}>Add people to this program. Each person can be in max 1 active program.</div>

      <div style={{ display: "flex", gap: 10, marginBottom: 20 }}>
        {[
          { id: "select", icon: "👥", label: "Select from team", desc: "Choose from existing members" },
          { id: "csv", icon: "📄", label: "Import CSV", desc: "Upload a list of emails" },
          { id: "invite", icon: "✉️", label: "Invite by email", desc: "Send invitation links" },
        ].map(m => (
          <Card key={m.id} selected={method === m.id} onClick={() => setMethod(m.id)} style={{ flex: 1 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ fontSize: 18 }}>{m.icon}</span>
              <div>
                <div style={{ fontSize: 13, fontWeight: 600, color: C.text }}>{m.label}</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>{m.desc}</div>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div style={{ marginBottom: 16 }}>
        <input placeholder="Search by name or email..." style={{
          width: "100%", padding: "10px 14px", border: `1px solid ${C.border}`, borderRadius: 8,
          fontSize: 13, boxSizing: "border-box", color: C.text,
        }} />
      </div>

      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 10, overflow: "hidden", marginBottom: 16 }}>
        {people.map((p, i) => (
          <div key={i} style={{
            display: "flex", justifyContent: "space-between", alignItems: "center",
            padding: "10px 16px", borderBottom: i < people.length - 1 ? `1px solid ${C.borderLight}` : "none",
            opacity: p.conflict ? 0.5 : 1,
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
              <div style={{
                width: 32, height: 32, borderRadius: 16, background: p.added ? C.primaryLight : C.borderLight,
                display: "flex", alignItems: "center", justifyContent: "center",
                fontSize: 13, fontWeight: 700, color: p.added ? C.primary : C.textMuted,
              }}>{p.name[0]}</div>
              <div>
                <div style={{ fontSize: 13, fontWeight: 500, color: C.text }}>{p.name}</div>
                <div style={{ fontSize: 11, color: p.conflict ? C.orange : C.textMuted }}>
                  {p.conflict ? `⚠️ Already in: ${p.conflict}` : p.email}
                </div>
              </div>
            </div>
            {p.conflict ? (
              <div style={{ padding: "4px 12px", borderRadius: 6, fontSize: 11, fontWeight: 600, color: C.orange, background: C.orangeLight }}>Unavailable</div>
            ) : (
              <div style={{
                padding: "4px 12px", borderRadius: 6, fontSize: 12, fontWeight: 600, cursor: "pointer",
                background: p.added ? C.greenLight : C.borderLight, color: p.added ? C.green : C.textMuted,
              }}>{p.added ? "✓ Added" : "+ Add"}</div>
            )}
          </div>
        ))}
      </div>

      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <span style={{ fontSize: 13, fontWeight: 600, color: C.text }}>4 participants selected</span>
        <Btn small outline>+ Invite new by email</Btn>
      </div>

      <Note>ℹ️ Participants will receive an enrollment notification with the program goal and their first step: choosing a personal focus area. Manager assignment is in the next step.</Note>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   STEP 3: EXPERIENCE & SETTINGS (merged)
   Formerly Steps 3 + 4. Visibility is now a
   collapsible "Advanced" section with defaults.
   ═══════════════════════════════════════════════ */
const Step3 = () => {
  const [coachModel, setCoachModel] = useState("choose");
  const [microActions, setMicroActions] = useState(true);
  const [showAdvanced, setShowAdvanced] = useState(false);

  return (
    <div>
      <div style={{ fontSize: 18, fontWeight: 700, color: C.text, marginBottom: 4 }}>Experience & Settings</div>
      <div style={{ fontSize: 13, color: C.textSec, marginBottom: 24 }}>Define how the program runs and what's visible to whom.</div>

      {/* ── Sessions ── */}
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 10 }}>Coaching sessions</label>
      <div style={{ display: "flex", gap: 16, marginBottom: 24 }}>
        <div style={{ flex: 1 }}><Input label="Sessions per participant" value="5" hint="Total within program duration" /></div>
        <div style={{ flex: 1 }}><Input label="Recommended cadence" value="Every 2–3 weeks" hint="Shown to participants as guidance" /></div>
      </div>

      {/* ── Expert assignment (renamed from coaches) ── */}
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 10 }}>How are experts assigned?</label>
      <div style={{ display: "flex", gap: 10, marginBottom: 24 }}>
        {[
          { id: "choose", icon: "🔍", label: "Participant chooses", desc: "From full marketplace" },
          { id: "shortlist", icon: "📋", label: "HR shortlist", desc: "You pre-select, they pick" },
        ].map(m => (
          <Card key={m.id} selected={coachModel === m.id} onClick={() => setCoachModel(m.id)} style={{ flex: 1 }}>
            <div style={{ fontSize: 18, marginBottom: 4 }}>{m.icon}</div>
            <div style={{ fontSize: 13, fontWeight: 600, color: C.text }}>{m.label}</div>
            <div style={{ fontSize: 11, color: C.textMuted, marginTop: 2 }}>{m.desc}</div>
          </Card>
        ))}
      </div>
      {coachModel === "shortlist" && (
        <div style={{ marginBottom: 24 }}>
          <input placeholder="Search experts to add to shortlist..." style={{
            width: "100%", padding: "10px 14px", border: `1px solid ${C.border}`, borderRadius: 8,
            fontSize: 13, boxSizing: "border-box", color: C.text, maxWidth: 400,
          }} />
          <div style={{ marginTop: 8, display: "flex", gap: 6 }}>
            <Chip selected>Dana Brožková</Chip>
            <Chip selected>Adam Joseph</Chip>
          </div>
        </div>
      )}

      {/* ── Per-participant manager assignment (NEW) ── */}
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 4 }}>Assign manager to each participant</label>
      <div style={{ fontSize: 11, color: C.textMuted, marginBottom: 12 }}>Manager gets read-only access to Programs tab for their assigned participant only. Optional per row.</div>
      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 10, overflow: "hidden", marginBottom: 24 }}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", padding: "8px 16px", background: C.bg, borderBottom: `1px solid ${C.border}` }}>
          <span style={{ fontSize: 12, fontWeight: 700, color: C.text }}>Participant</span>
          <span style={{ fontSize: 12, fontWeight: 700, color: C.text }}>Manager</span>
        </div>
        {[
          { name: "Tomáš Novák", manager: "Petr Motloch" },
          { name: "Jana Dvořáková", manager: "Petr Motloch" },
          { name: "Petr Svoboda", manager: "—" },
          { name: "Markéta Černá", manager: "Martin Smith" },
        ].map((row, i) => (
          <div key={i} style={{
            display: "grid", gridTemplateColumns: "1fr 1fr", padding: "10px 16px",
            borderBottom: i < 3 ? `1px solid ${C.borderLight}` : "none", alignItems: "center",
          }}>
            <span style={{ fontSize: 13, color: C.text }}>{row.name}</span>
            <div style={{
              padding: "6px 12px", borderRadius: 6, border: `1px solid ${C.border}`,
              fontSize: 12, color: row.manager === "—" ? C.textMuted : C.text, background: C.white,
              display: "flex", justifyContent: "space-between", alignItems: "center",
            }}>
              {row.manager} <span style={{ fontSize: 10, color: C.textMuted }}>▼</span>
            </div>
          </div>
        ))}
      </div>

      {/* ── Micro-actions: simple toggle + example ── */}
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 4 }}>Weekly micro-actions</label>
      <div style={{ display: "flex", gap: 10, marginBottom: 8 }}>
        <Card selected={microActions} onClick={() => setMicroActions(true)} style={{ flex: 1 }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: C.text }}>✓ Enabled</div>
          <div style={{ fontSize: 11, color: C.textMuted, marginTop: 2 }}>Weekly practice prompts between sessions</div>
        </Card>
        <Card selected={!microActions} onClick={() => setMicroActions(false)} style={{ flex: 1 }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: C.text }}>Disabled</div>
          <div style={{ fontSize: 11, color: C.textMuted, marginTop: 2 }}>Sessions only, no between-session actions</div>
        </Card>
      </div>
      {microActions && (
        <div style={{
          background: C.primaryLight, borderRadius: 8, padding: "10px 14px", marginBottom: 8,
          fontSize: 12, color: C.primaryDark, lineHeight: 1.6,
        }}>
          Participants receive one AI-generated weekly practice prompt based on their focus area and coaching sessions.<br />
          <span style={{ fontStyle: "italic", opacity: 0.8 }}>Example: "This week, practice giving specific positive feedback to one team member after at least 2 meetings."</span>
        </div>
      )}

      {/* ── Weekly cycle visual ── */}
      <div style={{
        display: "flex", alignItems: "center", gap: 0, margin: "16px 0 24px",
        background: C.white, border: `1px solid ${C.border}`, borderRadius: 10, padding: "14px 10px",
      }}>
        {[
          { icon: "🎯", label: "Session", sub: "with coach" },
          null,
          { icon: "🤖", label: "AI Action", sub: "weekly prompt" },
          null,
          { icon: "✅", label: "Practice", sub: "participant does" },
          null,
          { icon: "📊", label: "Evidence", sub: "HR sees data" },
        ].map((item, i) => (
          <div key={i} style={{ flex: item === null ? 0 : 1, textAlign: "center" }}>
            {item === null ? (
              <span style={{ color: C.border, fontSize: 18, padding: "0 4px" }}>→</span>
            ) : (
              <>
                <div style={{ fontSize: 20, marginBottom: 4 }}>{item.icon}</div>
                <div style={{ fontSize: 11, fontWeight: 600, color: C.text }}>{item.label}</div>
                <div style={{ fontSize: 10, color: C.textMuted }}>{item.sub}</div>
              </>
            )}
          </div>
        ))}
      </div>

      {/* ── Checkpoints ── */}
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 4 }}>Program checkpoints</label>
      <div style={{ fontSize: 11, color: C.textMuted, marginBottom: 12 }}>Built-in moments when participants reflect and HR gets measurable data.</div>
      <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 10, overflow: "hidden", marginBottom: 24 }}>
        {[
          { name: "Enrollment", when: "Day 1", what: "Focus area, personal goal, baseline self-assessment" },
          { name: "Mid-checkpoint", when: "~Day 45", what: "Progress reflection (5–7 questions), \"What's been most useful?\"" },
          { name: "Final review", when: "~Day 90", what: "End assessment, biggest change noticed, NPS" },
        ].map((cp, i) => (
          <div key={i} style={{
            display: "grid", gridTemplateColumns: "120px 70px 1fr 60px",
            padding: "10px 16px", fontSize: 12.5, alignItems: "center",
            borderBottom: i < 2 ? `1px solid ${C.borderLight}` : "none",
          }}>
            <span style={{ fontWeight: 600, color: C.text }}>{cp.name}</span>
            <span style={{ color: C.textMuted }}>{cp.when}</span>
            <span style={{ color: C.textSec }}>{cp.what}</span>
            <span style={{ color: C.green, fontSize: 11, fontWeight: 600 }}>Active</span>
          </div>
        ))}
      </div>

      {/* ── Advanced: Visibility & Privacy (collapsible) ── */}
      <div style={{
        border: `1px solid ${C.border}`, borderRadius: 10, overflow: "hidden",
        background: showAdvanced ? C.white : C.bg,
      }}>
        <div onClick={() => setShowAdvanced(!showAdvanced)} style={{
          padding: "12px 16px", cursor: "pointer",
          display: "flex", justifyContent: "space-between", alignItems: "center",
        }}>
          <div>
            <span style={{ fontSize: 13, fontWeight: 600, color: C.text }}>🔒 Visibility & Privacy</span>
            {!showAdvanced && <span style={{ fontSize: 11, color: C.textMuted, marginLeft: 8 }}>Defaults applied · Click to customize</span>}
          </div>
          <span style={{ fontSize: 14, color: C.textMuted, transition: "transform 0.2s", display: "inline-block", transform: showAdvanced ? "rotate(180deg)" : "rotate(0)" }}>▼</span>
        </div>

        {showAdvanced && (
          <div style={{ padding: "0 16px 16px", borderTop: `1px solid ${C.borderLight}` }}>
            <Note type="warning">⚠️ These settings are shown to participants during enrollment for transparency.</Note>

            <label style={{ display: "block", fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 8, marginTop: 8 }}>HR Admin sees:</label>
            <div style={{ marginBottom: 16 }}>
              {[
                { label: "Session attendance & utilization", checked: true, locked: true },
                { label: "Goal completion status", checked: true, locked: true },
                { label: "Micro-action completion rate", checked: true, locked: false },
                { label: "Checkpoint self-reflection responses", checked: false, locked: false },
                { label: "Individual assessment results", checked: false, locked: false },
              ].map((item, i) => (
                <div key={i} style={{
                  display: "flex", alignItems: "center", gap: 10, padding: "6px 0",
                  borderBottom: i < 4 ? `1px solid ${C.borderLight}` : "none",
                }}>
                  <div style={{
                    width: 18, height: 18, borderRadius: 3, flexShrink: 0,
                    border: `2px solid ${item.checked ? C.primary : C.border}`,
                    background: item.checked ? C.primary : "transparent",
                    display: "flex", alignItems: "center", justifyContent: "center",
                    color: "#fff", fontSize: 11, cursor: item.locked ? "default" : "pointer",
                    opacity: item.locked ? 0.7 : 1,
                  }}>{item.checked ? "✓" : ""}</div>
                  <span style={{ fontSize: 12.5, color: C.text, flex: 1 }}>{item.label}</span>
                  {item.locked && <span style={{ fontSize: 10, color: C.textMuted, fontStyle: "italic" }}>Always on</span>}
                </div>
              ))}
            </div>

            <label style={{ display: "block", fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 8 }}>Manager sees:</label>
            <div style={{ marginBottom: 16 }}>
              {[
                { label: "Enrollment status (name + program)", checked: true, locked: true },
                { label: "Personal focus area & goal", checked: true, locked: false },
                { label: "Session attendance (yes/no, not content)", checked: true, locked: false },
                { label: "Goal progress (on track / at risk)", checked: false, locked: false },
              ].map((item, i) => (
                <div key={i} style={{
                  display: "flex", alignItems: "center", gap: 10, padding: "6px 0",
                  borderBottom: i < 3 ? `1px solid ${C.borderLight}` : "none",
                }}>
                  <div style={{
                    width: 18, height: 18, borderRadius: 3, flexShrink: 0,
                    border: `2px solid ${item.checked ? C.primary : C.border}`,
                    background: item.checked ? C.primary : "transparent",
                    display: "flex", alignItems: "center", justifyContent: "center",
                    color: "#fff", fontSize: 11, cursor: item.locked ? "default" : "pointer",
                  }}>{item.checked ? "✓" : ""}</div>
                  <span style={{ fontSize: 12.5, color: C.text, flex: 1 }}>{item.label}</span>
                  {item.locked && <span style={{ fontSize: 10, color: C.textMuted, fontStyle: "italic" }}>Always on</span>}
                </div>
              ))}
            </div>

            <label style={{ display: "block", fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 8 }}>Never visible:</label>
            <div style={{ background: C.redLight, borderRadius: 8, padding: 12 }}>
              {["Coaching session content & notes", "Personal reflections & journal entries", "Private messages between participant and coach"].map((item, i) => (
                <div key={i} style={{ display: "flex", alignItems: "center", gap: 8, padding: "4px 0", fontSize: 12, color: C.red }}>
                  <span>🔒</span> {item}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

/* ═══════════════════════════════════════════════
   STEP 4: REVIEW & LAUNCH
   ═══════════════════════════════════════════════ */
const Step4 = ({ onPublish }) => (
  <div>
    <div style={{ fontSize: 18, fontWeight: 700, color: C.text, marginBottom: 4 }}>Review & Launch</div>
    <div style={{ fontSize: 13, color: C.textSec, marginBottom: 24 }}>Review your program before launching.</div>

    <div style={{ background: C.white, border: `1px solid ${C.border}`, borderRadius: 12, overflow: "hidden" }}>
      {[
        { section: "Program", items: [
          { l: "Name", v: "Leadership Development Q1 2026" },
          { l: "Goal", v: "Improve feedback culture & accountability for Team Leads" },
          { l: "Target group", v: "Team Leads, Engineering division" },
          { l: "Template", v: "Leadership 90d" },
          { l: "Duration", v: "90 days · Mar 15 → Jun 13, 2026" },
          { l: "Focus areas", v: "Giving feedback, Delegation" },
        ]},
        { section: "Participants", items: [
          { l: "Count", v: "4 participants" },
          { l: "Managers", v: "Petr Motloch (2), Martin Smith (1), unassigned (1)" },
        ]},
        { section: "Experience", items: [
          { l: "Sessions", v: "5 per participant · Every 2–3 weeks" },
          { l: "Expert model", v: "Participant chooses from marketplace" },
          { l: "Micro-actions", v: "Enabled (weekly AI-generated prompts)" },
          { l: "Checkpoints", v: "Enrollment · Mid (Day 45) · Final (Day 90)" },
        ]},
        { section: "Visibility", items: [
          { l: "HR sees", v: "Attendance, goal status, micro-action rate" },
          { l: "Manager sees", v: "Enrollment, focus area, attendance" },
          { l: "Protected", v: "Session content, reflections, messages" },
        ]},
      ].map((s, si) => (
        <div key={si}>
          <div style={{
            padding: "10px 18px", background: C.bg, fontSize: 12, fontWeight: 700,
            color: C.text, textTransform: "uppercase", letterSpacing: 0.5,
            borderTop: si > 0 ? `1px solid ${C.border}` : "none",
            display: "flex", justifyContent: "space-between", alignItems: "center",
          }}>
            {s.section}
            <span style={{ color: C.primary, fontSize: 11, fontWeight: 600, cursor: "pointer", textTransform: "none" }}>Edit</span>
          </div>
          {s.items.map((item, ii) => (
            <div key={ii} style={{
              display: "flex", padding: "8px 18px", fontSize: 13,
              borderBottom: `1px solid ${C.borderLight}`,
            }}>
              <span style={{ width: 140, color: C.textMuted, flexShrink: 0 }}>{item.l}</span>
              <span style={{ color: C.text }}>{item.v}</span>
            </div>
          ))}
        </div>
      ))}
    </div>

    <div style={{ marginTop: 24, display: "flex", gap: 12, alignItems: "center" }}>
      <Btn primary onClick={onPublish} style={{ padding: "12px 32px", fontSize: 14 }}>🚀 Launch Program</Btn>
      <Btn outline>Save as Draft</Btn>
      <span style={{ fontSize: 12, color: C.textMuted, marginLeft: 8 }}>Participants will be notified on the start date (Mar 15)</span>
    </div>
  </div>
);

/* ═══════════════════════════════════════════════
   WIZARD SHELL — 4 STEPS
   ═══════════════════════════════════════════════ */
const steps = [
  { id: 1, label: "Setup" },
  { id: 2, label: "Participants" },
  { id: 3, label: "Experience & Settings" },
  { id: 4, label: "Review & Launch" },
];

export default function CreateProgramWizard() {
  const [step, setStep] = useState(1);
  const [launched, setLaunched] = useState(false);

  if (launched) {
    return (
      <div style={{ fontFamily: "-apple-system, 'Segoe UI', sans-serif", background: C.bg, minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <div style={{ textAlign: "center", maxWidth: 440 }}>
          <div style={{ width: 80, height: 80, borderRadius: 40, background: C.greenLight, display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 20px", fontSize: 36 }}>🚀</div>
          <div style={{ fontSize: 22, fontWeight: 700, color: C.text, marginBottom: 8 }}>Program Launched!</div>
          <div style={{ fontSize: 14, color: C.textSec, lineHeight: 1.6, marginBottom: 24 }}>
            "Leadership Development Q1 2026" is ready.<br />4 participants will be notified on Mar 15, 2026.
          </div>
          <div style={{ display: "flex", gap: 10, justifyContent: "center" }}>
            <Btn primary onClick={() => { setLaunched(false); setStep(1); }}>View Program</Btn>
            <Btn outline onClick={() => { setLaunched(false); setStep(1); }}>Back to Programs</Btn>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ fontFamily: "-apple-system, 'Segoe UI', sans-serif", background: C.bg, minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      {/* Top bar */}
      <div style={{
        padding: "12px 24px", background: C.white, borderBottom: `1px solid ${C.border}`,
        display: "flex", justifyContent: "space-between", alignItems: "center",
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ width: 28, height: 28, borderRadius: 6, background: C.primary, display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontSize: 11, fontWeight: 800 }}>TL</div>
          <span style={{ fontSize: 15, fontWeight: 700, color: C.text }}>Create Program</span>
        </div>
        <Btn small>✕ Cancel</Btn>
      </div>

      <div style={{ display: "flex", flex: 1 }}>
        {/* Step sidebar */}
        <div style={{ width: 220, background: C.white, borderRight: `1px solid ${C.border}`, padding: "24px 16px" }}>
          {steps.map((s) => (
            <div key={s.id} onClick={() => setStep(s.id)} style={{
              display: "flex", alignItems: "center", gap: 10, padding: "10px 12px",
              borderRadius: 8, marginBottom: 4, cursor: "pointer",
              background: step === s.id ? C.primaryLight : "transparent",
            }}>
              <div style={{
                width: 26, height: 26, borderRadius: 13, fontSize: 12, fontWeight: 700, flexShrink: 0,
                display: "flex", alignItems: "center", justifyContent: "center",
                background: step === s.id ? C.primary : s.id < step ? C.green : C.borderLight,
                color: step === s.id || s.id < step ? "#fff" : C.textMuted,
              }}>{s.id < step ? "✓" : s.id}</div>
              <span style={{
                fontSize: 13, fontWeight: step === s.id ? 600 : 400,
                color: step === s.id ? C.primary : s.id < step ? C.green : C.textMuted,
              }}>{s.label}</span>
            </div>
          ))}

          <div style={{ marginTop: 32, padding: 12, background: C.primaryLight, borderRadius: 8, fontSize: 11, color: C.primaryDark, lineHeight: 1.5 }}>
            <strong>4 kroky.</strong> Non-lineární navigace. Validace až při Launch.
          </div>
        </div>

        {/* Content */}
        <div style={{ flex: 1, padding: "28px 40px", maxWidth: 720, overflow: "auto" }}>
          {step === 1 && <Step1 />}
          {step === 2 && <Step2 />}
          {step === 3 && <Step3 />}
          {step === 4 && <Step4 onPublish={() => setLaunched(true)} />}
        </div>
      </div>

      {/* Bottom bar */}
      <div style={{
        padding: "12px 24px", background: C.white, borderTop: `1px solid ${C.border}`,
        display: "flex", justifyContent: "space-between", alignItems: "center",
      }}>
        <Btn disabled={step === 1} onClick={() => setStep(Math.max(1, step - 1))}>← Back</Btn>
        <div style={{ display: "flex", gap: 8 }}>
          <Btn outline>Save Draft</Btn>
          {step < 4
            ? <Btn primary onClick={() => setStep(Math.min(4, step + 1))}>Continue →</Btn>
            : <Btn primary onClick={() => setLaunched(true)}>🚀 Launch Program</Btn>
          }
        </div>
      </div>

      {/* Designer notes */}
      <div style={{ background: "#F8F7FF", borderTop: `1px solid ${C.border}`, padding: "10px 24px" }}>
        <div style={{ fontSize: 11, color: C.primaryDark, lineHeight: 1.5 }}>
          <strong>Step {step}/4:</strong>{" "}
          {step === 1 && "Setup: Template předvyplní focus areas + duration. Focus areas: participant vybere 1 (ne 1–2, secondary goals vyřazeny z MVP). Název a goal povinné, zbytek optional. Duration podporuje 90/180/270/365 — interně dělí na 90d cykly."}
          {step === 2 && "Participants: Max 1 aktivní program per participant — pokud je osoba už v programu, zobrazí se 'Unavailable' s názvem programu. Po Launch NELZE přidávat další participanty (tomuto MVP omezení se vyhneme late-joiner edge cases). Manager assignment v Step 3."}
          {step === 3 && "Experience & Settings: Expert model (2 možnosti: choose/shortlist). Per-participant manager tabulka. Micro-actions toggle + příklad + weekly cycle diagram. Checkpoints auto-calculated z duration (mid-cycle ~D45, cycle review ~D90). Visibility collapsible Advanced."}
          {step === 4 && "Review: Validace při Launch: název, goal, ≥1 participant, sessions. Po Launch → success → Program Detail. Save Draft → uloží bez notifikace. Participanti dostanou enrollment email na start date."}
        </div>
      </div>
    </div>
  );
}
