#!/usr/bin/env python3
"""Hub & spoke network architecture with security zones as subnets.
Azure hub (2 spokes) + GCP hub (1 spoke). Randomized RFC1918 space.
Management services (DNS, Jump, Active Directory) centralized in each hub."""
import random
from html import escape

random.seed(20260810)

# ---- randomized RFC1918 /16 allocations (generous) ----
az_octets = random.sample(range(1, 60), 3)
gcp_octets = random.sample(range(16, 32), 2)
AZ_HUB   = f"10.{az_octets[0]}"
AZ_SP1   = f"10.{az_octets[1]}"
AZ_SP2   = f"10.{az_octets[2]}"
GCP_HUB  = f"172.{gcp_octets[0]}"
GCP_SP1  = f"172.{gcp_octets[1]}"

# ---- palette ----
AZ, AZ_L = "#0078D4", "#E8F2FB"
GCP, GCP_L = "#188038", "#E9F5EC"
HUB, FW, FW_L = "#37474F", "#C62828", "#FDEAEA"
INK, MUT = "#1A1A1A", "#5b6b76"
Z_DMZ, Z_APP, Z_DATA, Z_CHD, Z_MGMT = "#E65100", "#1565C0", "#8E24AA", "#B8860B", "#00838F"
ZL = {Z_DMZ:"#FDEDE1", Z_APP:"#E7EFFA", Z_DATA:"#F3E9F8", Z_CHD:"#F7F0DE", Z_MGMT:"#E1F1F2"}

def esc(s): return escape(str(s))

class SVG:
    def __init__(self, w, h, label):
        self.w, self.h = w, h
        self.s = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" '
                  f'width="{w}" height="{h}" role="img" aria-label="{esc(label)}">']
        self.s.append(f'<rect x="0" y="0" width="{w}" height="{h}" fill="#ffffff"/>')
    def rect(self, x, y, w, h, fill, stroke="none", sw=1, rx=8, dash=None):
        d = f' stroke-dasharray="{dash}"' if dash else ""
        self.s.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" rx="{rx}" '
                      f'fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')
    def text(self, x, y, t, size=13, color=INK, weight="normal", anchor="middle", style="normal"):
        self.s.append(f'<text x="{x:.1f}" y="{y:.1f}" font-size="{size}" fill="{color}" '
                      f'font-weight="{weight}" text-anchor="{anchor}" font-style="{style}" '
                      f'font-family="Segoe UI, Helvetica, Arial, sans-serif">{esc(t)}</text>')
    def line(self, x1, y1, x2, y2, color, w=2.4, dash=None):
        d = f' stroke-dasharray="{dash}"' if dash else ""
        self.s.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" '
                      f'stroke="{color}" stroke-width="{w}"{d} stroke-linecap="round"/>')
    def done(self):
        self.s.append("</svg>")
        return "\n".join(self.s)

# ---- zone/subnet model (spokes) ----
def subnet(name, cidr, hosts): return {"name": name, "cidr": cidr, "hosts": hosts}
def zone(name, color, subnets): return {"name": name, "color": color, "subnets": subnets}
def row_h(sn): return 30 + max(1, len(sn["hosts"])) * 15 + 12
def zone_h(z): return 28 + sum(row_h(s) for s in z["subnets"]) + 8
def spoke_h(zones): return 54 + sum(zone_h(z) for z in zones) + 8 * (len(zones) - 1) + 12

def make_spoke(base, has_app2):
    app_hosts = ["App-1: Backend API"] + (["App-2: Backend Service API"] if has_app2 else [])
    data_hosts = ["App-2: Database"] if has_app2 else ["(reserved)"]
    return [
        zone("DMZ Zone", Z_DMZ, [
            subnet("DMZ", f"{base}.0.0/24", ["App-1: Frontend UI"])]),
        zone("App Zone", Z_APP, [
            subnet("Application", f"{base}.10.0/24", app_hosts),
            subnet("App \u2013 Storage", f"{base}.11.0/24", ["App-1: Storage Account"])]),
        zone("Data Stores Zone", Z_DATA, [
            subnet("Data Stores", f"{base}.20.0/24", data_hosts)]),
        zone("Cardholder Data Zone", Z_CHD, [
            subnet("Cardholder Data (CDE)", f"{base}.30.0/24", ["(reserved \u2013 PCI CDE)"])]),
    ]

# ---- hub layout constants ----
MB_H, MB_GAP = 40, 8          # management subnet box height / gap
BOXES_H = 3 * MB_H + 2 * MB_GAP
HUB_H = 30 + BOXES_H + 16

# =========================================================
# canvas sizing (computed)
HUB_Y = 104
SP_Y = HUB_Y + HUB_H + 16
spoke_heights = [spoke_h(make_spoke(AZ_SP1, True)),
                 spoke_h(make_spoke(AZ_SP2, False)),
                 spoke_h(make_spoke(GCP_SP1, True))]
MAX_SP_H = max(spoke_heights)
REGION_TOP = 70
REGION_H = (SP_Y - REGION_TOP) + MAX_SP_H + 20
W = 1680
H = REGION_TOP + REGION_H + 46

g = SVG(W, H, "Hub and spoke network architecture with centralized management")
g.text(W/2, 32, "Hub & Spoke Network Architecture \u2014 Security Zones + Centralized Hub Management",
       size=22, weight="bold")
g.text(W/2, 54, "Azure hub (2 spokes) + GCP hub (1 spoke) \u2022 DNS / Jump / AD centralized in each hub "
                "\u2022 App-1 in all 3 LZs, App-2 in LZ1 + LZ3", size=13, color=MUT)

# region containers
g.rect(20, REGION_TOP, 1120, REGION_H, AZ_L, AZ, 2.4, rx=14)
g.text(38, 100, "AZURE  \u2022  West Europe", size=17, color=AZ, weight="bold", anchor="start")
g.rect(1152, REGION_TOP, 508, REGION_H, GCP_L, GCP, 2.4, rx=14)
g.text(1170, 100, "GCP  \u2022  us-central1", size=17, color=GCP, weight="bold", anchor="start")

# ---- hub with firewall + 3 management subnets (DNS / Jump / AD) ----
def draw_hub(x, y, w, title, base, fw_w):
    g.rect(x, y, w, HUB_H, "#fff", HUB, 2, rx=8)
    g.text(x + w/2, y + 22, title, size=15, color=HUB, weight="bold")
    # firewall (spans the 3-box height)
    fx, fy = x + 16, y + 30
    g.rect(fx, fy, fw_w, BOXES_H, FW_L, FW, 1.8, rx=6)
    g.text(fx + fw_w/2, fy + BOXES_H/2 - 16, "Firewall (NGFW)", size=13, color=FW, weight="bold")
    g.text(fx + fw_w/2, fy + BOXES_H/2 + 3, f"Transit {base}.0.0/24", size=10.5)
    g.text(fx + fw_w/2, fy + BOXES_H/2 + 18, f"Mgmt {base}.1.0/24", size=10.5)
    # management subnets stacked to the right
    rx0 = fx + fw_w + 14
    rw = (x + w - 16) - rx0
    mgmt = [("DNS Server", f"{base}.10.0/24"),
            ("Jump Server", f"{base}.11.0/24"),
            ("Active Directory", f"{base}.12.0/24")]
    for i, (nm, cidr) in enumerate(mgmt):
        by = fy + i * (MB_H + MB_GAP)
        g.rect(rx0, by, rw, MB_H, "#fff", Z_MGMT, 1.4, rx=6)
        g.rect(rx0, by, 6, MB_H, Z_MGMT, "none", 0, rx=0)
        g.text(rx0 + 14, by + 17, "Mgmt \u2013 " + nm.split()[0]
               if nm != "Active Directory" else "Mgmt \u2013 AD",
               size=11.5, color=Z_MGMT, weight="bold", anchor="start")
        g.text(rx0 + rw - 12, by + 17, cidr, size=11, color=INK, anchor="end")
        g.text(rx0 + 14, by + 32, "\u2022 " + nm, size=10.5, color=INK, weight="bold", anchor="start")

draw_hub(40, HUB_Y, 1080, f"AZURE HUB VNet   {AZ_HUB}.0.0/16", AZ_HUB, fw_w=250)
draw_hub(1168, HUB_Y, 476, f"GCP HUB VPC   {GCP_HUB}.0.0/16", GCP_HUB, fw_w=190)

# inter-hub peering
g.line(1120, HUB_Y + HUB_H/2, 1168, HUB_Y + HUB_H/2, "#8E24AA", 2.6, dash="7 4")
g.text(1144, HUB_Y + HUB_H/2 - 8, "inter-region", size=9, color="#8E24AA", weight="bold")

# ---- spoke renderer ----
def draw_spoke(x, y, w, title, cidr, region_color, zones):
    h = spoke_h(zones)
    g.rect(x, y, w, h, "#fff", region_color, 1.8, rx=10)
    g.text(x + w/2, y + 24, title, size=14.5, color=region_color, weight="bold")
    g.text(x + w/2, y + 42, f"VNet/VPC {cidr}", size=12, color=MUT, weight="bold")
    zy = y + 54
    for z in zones:
        zh = zone_h(z)
        g.rect(x + 12, zy, w - 24, zh, ZL[z["color"]], z["color"], 1.6, rx=8)
        g.text(x + 22, zy + 19, z["name"], size=12.5, color=z["color"], weight="bold", anchor="start")
        sy = zy + 28
        for sn in z["subnets"]:
            rh = row_h(sn)
            g.rect(x + 22, sy, w - 44, rh, "#fff", z["color"], 1.3, rx=6)
            g.rect(x + 22, sy, 6, rh, z["color"], "none", 0, rx=0)
            g.text(x + 34, sy + 17, sn["name"], size=12, color=z["color"], weight="bold", anchor="start")
            g.text(x + w - 56, sy + 17, sn["cidr"], size=11, color=INK, anchor="end")
            hy = sy + 33
            for host in sn["hosts"]:
                reserved = host.startswith("(")
                g.text(x + 34, hy, ("\u2022 " + host), size=10.5,
                       color=MUT if reserved else INK,
                       weight="normal" if reserved else "bold",
                       anchor="start", style="italic" if reserved else "normal")
                hy += 15
            sy += rh
        zy += zh + 8
    return h

draw_spoke(40, SP_Y, 528, "Landing Zone 1 (Spoke)  \u2013  App-1 + App-2",
           f"{AZ_SP1}.0.0/16", AZ, make_spoke(AZ_SP1, True))
draw_spoke(596, SP_Y, 528, "Landing Zone 2 (Spoke)  \u2013  App-1",
           f"{AZ_SP2}.0.0/16", AZ, make_spoke(AZ_SP2, False))
draw_spoke(1168, SP_Y, 476, "Landing Zone 3 (Spoke)  \u2013  App-1 + App-2",
           f"{GCP_SP1}.0.0/16", GCP, make_spoke(GCP_SP1, True))

# peering connectors hub -> spokes
for cx, col in [(304, AZ), (860, AZ), (1406, GCP)]:
    g.line(cx, HUB_Y + HUB_H, cx, SP_Y, col, 2.4)
    g.text(cx, SP_Y - 6, "peer", size=9, color=col, style="italic")

# legend
lx, ly = 40, H - 22
g.text(lx, ly, "Security zones:", size=12, weight="bold", anchor="start")
lx = 165
for lab, col in [("DMZ", Z_DMZ), ("App", Z_APP), ("Data Stores", Z_DATA),
                 ("Cardholder Data (CDE)", Z_CHD), ("Management (Hub)", Z_MGMT), ("Hub firewall", FW)]:
    g.rect(lx, ly - 11, 14, 14, col, "none", 0, rx=3)
    g.text(lx + 20, ly, lab, size=11.5, anchor="start")
    lx += len(lab) * 7.2 + 46

svg = g.done()
with open("assets/network_architecture.svg", "w", encoding="utf-8") as f:
    f.write('<?xml version="1.0" encoding="UTF-8"?>\n' + svg)
print("Azure hub  :", f"{AZ_HUB}.0.0/16", "| DNS", f"{AZ_HUB}.10.0/24", "Jump", f"{AZ_HUB}.11.0/24", "AD", f"{AZ_HUB}.12.0/24")
print("Azure LZ1  :", f"{AZ_SP1}.0.0/16")
print("Azure LZ2  :", f"{AZ_SP2}.0.0/16")
print("GCP hub    :", f"{GCP_HUB}.0.0/16", "| DNS", f"{GCP_HUB}.10.0/24", "Jump", f"{GCP_HUB}.11.0/24", "AD", f"{GCP_HUB}.12.0/24")
print("GCP LZ3    :", f"{GCP_SP1}.0.0/16")
print("canvas     :", W, "x", H, "| svg bytes", len(svg))
