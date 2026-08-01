import os
import sys
import subprocess
from pathlib import Path

# Fix terminal encoding for Windows console unicode display
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

PROJECT_DIR = Path(__file__).parent.resolve()

def find_docker_bin():
    """Trouve le binaire docker dans PATH ou dans les dossiers d'installation Windows par défaut."""
    try:
        res = subprocess.run(["docker", "--version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if res.returncode == 0:
            return "docker"
    except Exception:
        pass

    possible_paths = [
        Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "DockerDesktop" / "resources" / "bin" / "docker.exe",
        Path(os.environ.get("ProgramFiles", "")) / "Docker" / "Docker" / "resources" / "bin" / "docker.exe",
        Path("C:/Program Files/Docker/Docker/resources/bin/docker.exe")
    ]

    for path in possible_paths:
        if path.exists():
            return str(path)

    return "docker"

def stop_docker_containers():
    """Arrête et supprime les conteneurs Docker de BAOU Finance."""
    print("=" * 60)
    print("   🛑 BAOU FINANCE v2.0 — Arrêt des Conteneurs Docker   ")
    print("=" * 60)
    print(f"📁 Dossier du projet : {PROJECT_DIR}\n")

    docker_bin = find_docker_bin()
    print(f"🔍 Executable Docker : {docker_bin}")
    print("⏳ Arrêt en cours des serveurs Docker (docker compose down)...")

    try:
        res = subprocess.run([docker_bin, "compose", "down"], cwd=str(PROJECT_DIR), text=True)
        if res.returncode == 0:
            print("\n✅ Tous les conteneurs BAOU Finance (Backend & Admin) ont été arrêtés avec succès !")
        else:
            print(f"\n⚠️ Code de retour de la commande : {res.returncode}")
    except Exception as e:
        print(f"\n❌ Erreur lors de l'arrêt des conteneurs : {e}")

if __name__ == "__main__":
    stop_docker_containers()
