#!/usr/bin/env python3
"""Generate README.md (Markdown) for a GitHub repo directory.
Diagram embedded at top; full addressing plan as Markdown tables.
Addressing recomputed with the same seed as build_arch.py so it always matches."""
import random, os

random.seed(20260810)
az = random.sample(range(1, 60), 3)
gcp = random.sample(range(16, 32), 2)
AZ_HUB, AZ_SP1, AZ_SP2 = f"10.{az[0]}", f"10.{az[1]}", f"10.{az[2]}"
GCP_HUB, GCP_SP1 = f"172.{gcp[0]}", f"172.{gcp[1]}"

def spoke_rows(base, has_app2):
    app_hosts = "App-1: Backend API" + (" · App-2: Backend Service API" if has_app2 else "")
    data_hosts = "App-2: Database" if has_app2 else "— *(reserved)*"
    return [
        ("DMZ Zone", "DMZ", f"{base}.0.0/24", "App-1: Frontend UI"),
        ("App Zone", "Application", f"{base}.10.0/24", app_hosts),
        ("App Zone", "App – Storage", f"{base}.11.0/24", "App-1: Storage Account"),
        ("Data Stores Zone", "Data Stores", f"{base}.20.0/24", data_hosts),
        ("Cardholder Data Zone", "Cardholder Data (CDE)", f"{base}.30.0/24", "— *(reserved · PCI CDE)*"),
    ]

def hub_rows(base):
    return [
        ("Firewall", "Transit", f"{base}.0.0/24", "NGFW transit interface"),
        ("Firewall", "Management", f"{base}.1.0/24", "NGFW management interface"),
        ("Management", "Mgmt – DNS", f"{base}.10.0/24", "DNS Server"),
        ("Management", "Mgmt – Jump", f"{base}.11.0/24", "Jump Server"),
        ("Management", "Mgmt – AD", f"{base}.12.0/24", "Active Directory"),
    ]

def md_table(rows, headers=("Security zone", "Subnet", "CIDR", "Hosts")):
    out = ["| " + " | ".join(headers) + " |",
           "|" + "|".join(["---"] * len(headers)) + "|"]
    for zone, subnet, cidr, hosts in rows:
        out.append(f"| {zone} | {subnet} | `{cidr}` | {hosts} |")
    return "\n".join(out)

def scope(title, supernet, rows):
    return f"\n#### {title} &mdash; `{supernet}`\n\n{md_table(rows)}\n"

readme = f"""# Hub &amp; Spoke Network Architecture

Multi-cloud hub-and-spoke reference across **Azure (West Europe)** and **GCP (us-central1 / Iowa)**.
Security zones are implemented as subnets, and management services (DNS, Jump, Active Directory)
are centralized in each hub.

![Hub & Spoke Network Architecture](assets/network_architecture.png)

> Vector source: [`assets/network_architecture.svg`](assets/network_architecture.svg) &nbsp;·&nbsp;
> High-res raster: [`assets/network_architecture.png`](assets/network_architecture.png)

## Topology

- **Azure hub** (`{AZ_HUB}.0.0/16`) &rarr; 2 spokes: **LZ1** (`{AZ_SP1}.0.0/16`), **LZ2** (`{AZ_SP2}.0.0/16`)
- **GCP hub** (`{GCP_HUB}.0.0/16`) &rarr; 1 spoke: **LZ3** (`{GCP_SP1}.0.0/16`)
- **Application-1** exists in all three landing zones; **Application-2** in **LZ1 + LZ3**
- Security zones per spoke: **DMZ &rarr; App &rarr; Data Stores &rarr; Cardholder Data (CDE)**
- **DNS / Jump / Active Directory** centralized in each hub's Management subnets

## Component &rarr; Security Zone

| Component | Application | Security zone | Rationale |
|---|---|---|---|
| Frontend UI | App-1 | **DMZ** | External-facing tier |
| Backend API | App-1 | **App · Application** | Internal service tier |
| Backend Service API | App-2 | **App · Application** | Internal service tier |
| Storage Account | App-1 | **App · _separate subnet_** | Same zone as its Backend API, isolated subnet |
| Database | App-2 | **Data Stores** | Persistent data tier |
| DNS Server | Shared | **Management (Hub)** | Centralized in hub |
| Jump Server | Shared | **Management (Hub)** | Centralized in hub |
| Active Directory | Shared | **Management (Hub)** | Centralized in hub |

## Full Addressing Plan

Every VNet/VPC is a generously sized **`/16`**; security-zone subnets are **`/24`** (254 usable hosts each).

### Azure region
{scope(f"Azure Hub VNet", f"{AZ_HUB}.0.0/16", hub_rows(AZ_HUB))}
{scope(f"Landing Zone 1 (App-1 + App-2)", f"{AZ_SP1}.0.0/16", spoke_rows(AZ_SP1, True))}
{scope(f"Landing Zone 2 (App-1)", f"{AZ_SP2}.0.0/16", spoke_rows(AZ_SP2, False))}
### GCP region
{scope(f"GCP Hub VPC", f"{GCP_HUB}.0.0/16", hub_rows(GCP_HUB))}
{scope(f"Landing Zone 3 (App-1 + App-2)", f"{GCP_SP1}.0.0/16", spoke_rows(GCP_SP1, True))}
## Design Notes

- **Centralized management:** DNS, Jump, and Active Directory are hosted once per hub and consumed by all
  attached spokes over VNet/VPC peering &mdash; no per-spoke duplication.
- **Storage isolation:** the Storage Account sits in its own `/24` inside the App zone, alongside
  (but segmented from) the Backend API it serves.
- **DMZ:** the App-1 Frontend UI is the only external-facing tier and is placed in the DMZ zone of every
  landing zone.
- **Cardholder Data (CDE):** a dedicated PCI-scoped zone is reserved in every spoke, ready for regulated
  workloads with deny-by-default segmentation.
- **Generous sizing:** each VNet/VPC is a full `/16` (65k addresses); `/24` subnets leave ample headroom.

## Regenerating the diagram

The diagram is generated from [`build_arch.py`](build_arch.py) (Python + `cairosvg`). Addressing is derived
from a fixed random seed so the diagram and the tables above stay in sync.

```bash
python build_arch.py                              # writes assets/network_architecture.svg
python -c "import cairosvg; cairosvg.svg2png( \\
  url='assets/network_architecture.svg', \\
  write_to='assets/network_architecture.png', output_width=2400)"
python make_readme.py                             # regenerates this README
```

## Repository layout

```
network-architecture/
├── README.md                      # this file (diagram + addressing)
├── build_arch.py                  # diagram generator
├── make_readme.py                 # README generator
└── assets/
    ├── network_architecture.svg   # vector diagram
    └── network_architecture.png   # raster diagram (embedded above)
```
"""

os.makedirs("./assets", exist_ok=True)
with open("./README.md", "w", encoding="utf-8") as f:
    f.write(readme)
print("README bytes:", len(readme))
