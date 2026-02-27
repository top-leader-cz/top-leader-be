from diagrams import Diagram, Cluster, Edge
from diagrams.gcp.compute import Run
from diagrams.gcp.database import SQL
from diagrams.gcp.network import LoadBalancing, DNS, CDN
from diagrams.gcp.storage import GCS
from diagrams.gcp.security import KeyManagementService
from diagrams.gcp.devtools import ContainerRegistry
from diagrams.gcp.operations import Monitoring
from diagrams.onprem.client import User
from diagrams.onprem.ci import GithubActions
from diagrams.onprem.network import Internet
from diagrams.saas.chat import Slack
from diagrams.programming.language import Python

graph_attr = {
    "fontsize": "18",
    "bgcolor": "white",
    "pad": "0.5",
    "nodesep": "0.8",
    "ranksep": "1.2",
}

with Diagram(
    "TopLeader — GCP Architecture",
    filename="/Users/jakubkrhovjak/IdeaProjects/top-leader-be/docs/architecture",
    show=False,
    direction="TB",
    graph_attr=graph_attr,
    outformat="png",
):
    user = User("Browser")
    github = GithubActions("GitHub Actions\nCI/CD")

    dns = DNS("DNS\ntopleaderplatform.io\nqa.topleaderplatform.io")

    with Cluster("GCP — topleader-394306 · europe-west3"):

        with Cluster("Global L7 Load Balancer"):
            lb = LoadBalancing("HTTPS\nGoogle-managed SSL\npath-based routing")

        with Cluster("Cloud Run"):
            cr_qa = Run("QA\n0–1 inst · 600Mi")
            cr_prod = Run("PROD\n1–4 inst · 600Mi")

        with Cluster("Cloud Storage + CDN"):
            fe_qa = GCS("QA Frontend")
            fe_prod = GCS("PROD Frontend")
            ai_img = GCS("AI Images")
            cdn = CDN("CDN")

        with Cluster("VPC · 10.27.0.0/16 (private)"):
            db = SQL("Cloud SQL\nPostgreSQL 15\n10.27.0.3")

        ar = ContainerRegistry("Artifact\nRegistry")
        secrets = KeyManagementService("Secret\nManager")
        mon = Monitoring("Monitoring\n& Alerts")

    with Cluster("External Services"):
        openai = Internet("OpenAI\nGPT-4o")
        smtp = Internet("Gmail\nSMTP")
        google_oauth = Internet("Google\nOAuth")
        calendly = Internet("Calendly\nAPI")

    # User flow
    user >> dns >> lb
    lb >> Edge(label="/api/*") >> [cr_qa, cr_prod]
    lb >> Edge(label="/*") >> cdn >> [fe_qa, fe_prod]

    # Cloud Run → DB (private IP)
    cr_qa >> Edge(style="dashed", label="private IP") >> db
    cr_prod >> Edge(style="dashed", label="private IP") >> db

    # Cloud Run → external
    cr_prod >> [openai, smtp, google_oauth, calendly]

    # Cloud Run → secrets
    cr_qa >> Edge(style="dotted") >> secrets
    cr_prod >> Edge(style="dotted") >> secrets

    # Cloud Run → AI images
    cr_prod >> ai_img

    # Monitoring
    cr_prod >> Edge(style="dotted") >> mon

    # CI/CD
    github >> Edge(label="push image") >> ar
    ar >> Edge(label="deploy") >> cr_qa
    ar >> Edge(label="deploy") >> cr_prod
