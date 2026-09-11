# Hub &amp; Spoke Network Architecture

Multi-cloud hub-and-spoke reference across **Azure (West Europe)** and **GCP (us-central1 / Iowa)**.
Security zones are implemented as subnets, and management services (DNS, Jump, Active Directory)
are centralized in each hub.

![Hub & Spoke Network Architecture](docs/network_architecture.png)

> Vector source: [`docs/network_architecture.svg`](docs/network_architecture.svg) &nbsp;·&nbsp;
> High-res raster: [`docs/network_architecture.png`](docs/network_architecture.png)

## Topology

- **Azure hub** (`10.21.0.0/16`) &rarr; 2 spokes: **LZ1** (`10.4.0.0/16`), **LZ2** (`10.23.0.0/16`)
- **GCP hub** (`172.16.0.0/16`) &rarr; 1 spoke: **LZ3** (`172.23.0.0/16`)
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

#### Azure Hub VNet &mdash; `10.21.0.0/16`

| Security zone | Subnet | CIDR | Hosts |
|---|---|---|---|
| Firewall | Transit | `10.21.0.0/24` | NGFW transit interface |
| Firewall | Management | `10.21.1.0/24` | NGFW management interface |
| Management | Mgmt – DNS | `10.21.10.0/24` | DNS Server |
| Management | Mgmt – Jump | `10.21.11.0/24` | Jump Server |
| Management | Mgmt – AD | `10.21.12.0/24` | Active Directory |


#### Landing Zone 1 (App-1 + App-2) &mdash; `10.4.0.0/16`

| Security zone | Subnet | CIDR | Hosts |
|---|---|---|---|
| DMZ Zone | DMZ | `10.4.0.0/24` | App-1: Frontend UI |
| App Zone | Application | `10.4.10.0/24` | App-1: Backend API · App-2: Backend Service API |
| App Zone | App – Storage | `10.4.11.0/24` | App-1: Storage Account |
| Data Stores Zone | Data Stores | `10.4.20.0/24` | App-2: Database |
| Cardholder Data Zone | Cardholder Data (CDE) | `10.4.30.0/24` | — *(reserved · PCI CDE)* |


#### Landing Zone 2 (App-1) &mdash; `10.23.0.0/16`

| Security zone | Subnet | CIDR | Hosts |
|---|---|---|---|
| DMZ Zone | DMZ | `10.23.0.0/24` | App-1: Frontend UI |
| App Zone | Application | `10.23.10.0/24` | App-1: Backend API |
| App Zone | App – Storage | `10.23.11.0/24` | App-1: Storage Account |
| Data Stores Zone | Data Stores | `10.23.20.0/24` | — *(reserved)* |
| Cardholder Data Zone | Cardholder Data (CDE) | `10.23.30.0/24` | — *(reserved · PCI CDE)* |

### GCP region

#### GCP Hub VPC &mdash; `172.16.0.0/16`

| Security zone | Subnet | CIDR | Hosts |
|---|---|---|---|
| Firewall | Transit | `172.16.0.0/24` | NGFW transit interface |
| Firewall | Management | `172.16.1.0/24` | NGFW management interface |
| Management | Mgmt – DNS | `172.16.10.0/24` | DNS Server |
| Management | Mgmt – Jump | `172.16.11.0/24` | Jump Server |
| Management | Mgmt – AD | `172.16.12.0/24` | Active Directory |


#### Landing Zone 3 (App-1 + App-2) &mdash; `172.23.0.0/16`

| Security zone | Subnet | CIDR | Hosts |
|---|---|---|---|
| DMZ Zone | DMZ | `172.23.0.0/24` | App-1: Frontend UI |
| App Zone | Application | `172.23.10.0/24` | App-1: Backend API · App-2: Backend Service API |
| App Zone | App – Storage | `172.23.11.0/24` | App-1: Storage Account |
| Data Stores Zone | Data Stores | `172.23.20.0/24` | App-2: Database |
| Cardholder Data Zone | Cardholder Data (CDE) | `172.23.30.0/24` | — *(reserved · PCI CDE)* |

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
python build_arch.py                              # writes docs/network_architecture.svg
python -c "import cairosvg; cairosvg.svg2png( \
  url='docs/network_architecture.svg', \
  write_to='docs/network_architecture.png', output_width=2400)"
python make_readme.py                             # regenerates this README
```

## Repository layout

```
aud/example-data/example-architecture/
├── README.md                      # this file (diagram + addressing)
├── build_arch.py                  # diagram generator
├── make_readme.py                 # README generator
└── docs/
    ├── network_architecture.svg   # vector diagram
    └── network_architecture.png   # raster diagram (embedded above)
```

# Hub & Spoke Network Architecture — Addressing Reference

A static site documenting a multi-cloud hub-and-spoke network across **Azure (West Europe)** and **GCP (us-central1 / Iowa)**, with security zones implemented as subnets and centralized hub management (DNS, Jump, Active Directory).

## Contents

| File | Purpose |
|------|---------|
| `index.html` | The page. Diagram is embedded as **inline SVG**; includes the component→zone mapping and the **full addressing plan**. Self-contained, no build step. |
| `network_architecture.svg` | Standalone vector copy of the diagram (open in any browser / editor / draw.io). |
| `network_architecture.png` | High-resolution raster copy (docs, wikis, chat). |

## Topology at a glance

- **Azure hub** (`10.21.0.0/16`) → 2 spokes: **LZ1** (`10.4.0.0/16`), **LZ2** (`10.23.0.0/16`)
- **GCP hub** (`172.16.0.0/16`) → 1 spoke: **LZ3** (`172.23.0.0/16`)
- **App-1** in all three landing zones; **App-2** in LZ1 + LZ3
- Security zones per spoke: **DMZ → App → Data Stores → Cardholder Data (CDE)**
- **DNS / Jump / Active Directory** centralized in each hub's Management subnets

> Full per-subnet CIDRs and host assignments are in the addressing table on the page.

## Publish on GitHub Pages

1. Add these files to a repo root.
   ```bash
   git init
   git add index.html network_architecture.svg network_architecture.png README.md
   git commit -m "Add hub & spoke network architecture page"
   git branch -M main
   git remote add origin https://github.com/<your-user>/network-architecture.git
   git push -u origin main
   ```
2. **Settings -> Pages -> Source:** *Deploy from a branch* -> **`main`** / **`/ (root)`** -> Save.
3. Live at `https://<your-user>.github.io/network-architecture/` (~1 min).

## Preview locally

```bash
python -m http.server 8000   # then open http://localhost:8000
```

## Editing

- **Addressing / mapping:** edit the tables in `index.html`.
- **Diagram:** the vector is inline in `index.html` (and mirrored in `network_architecture.svg`).

