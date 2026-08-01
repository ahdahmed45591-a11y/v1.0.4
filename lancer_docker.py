import os
import sys
import time
import subprocess
import webbrowser
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
    # 1. Vérifier si 'docker' est disponible dans le PATH
    try:
        res = subprocess.run(["docker", "--version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if res.returncode == 0:
            return "docker"
    except Exception:
        pass

    # 2. Emplacements connus sur Windows
    possible_paths = [
        Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "DockerDesktop" / "resources" / "bin" / "docker.exe",
        Path(os.environ.get("ProgramFiles", "")) / "Docker" / "Docker" / "resources" / "bin" / "docker.exe",
        Path("C:/Program Files/Docker/Docker/resources/bin/docker.exe")
    ]

    for path in possible_paths:
        if path.exists():
            return str(path)

    return "docker"

def find_docker_desktop_app():
    """Trouve l'exécutable GUI de Docker Desktop."""
    possible_paths = [
        Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "DockerDesktop" / "Docker Desktop.exe",
        Path(os.environ.get("ProgramFiles", "")) / "Docker" / "Docker" / "Docker Desktop.exe",
        Path("C:/Program Files/Docker/Docker/Docker Desktop.exe")
    ]
    for path in possible_paths:
        if path.exists():
            return str(path)
    return None

def is_docker_engine_running(docker_bin):
    """Vérifie si le moteur Docker Engine répond (docker info)."""
    try:
        res = subprocess.run([docker_bin, "info"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=5)
        return res.returncode == 0
    except Exception:
        return False

def start_docker_desktop():
    """Lance l'application Docker Desktop si le moteur n'est pas actif."""
    app_path = find_docker_desktop_app()
    if app_path and os.path.exists(app_path):
        print(f"🚀 Lancement de Docker Desktop depuis : {app_path}")
        subprocess.Popen([app_path], shell=True)
    else:
        print("⚠️ Impossible de trouver Docker Desktop.exe automatiquement. Veuillez l'ouvrir manuellement.")

def wait_for_docker_engine(docker_bin, timeout_seconds=45):
    """Attend que le moteur Docker soit prêt."""
    print("⏳ Attente du démarrage de Docker Engine...", end="", flush=True)
    start_time = time.time()
    while time.time() - start_time < timeout_seconds:
        if is_docker_engine_running(docker_bin):
            print("\n✅ Docker Engine est actif et connecté !")
            return True
        print(".", end="", flush=True)
        time.sleep(2.5)
    print("\n❌ Temps d'attente dépassé. Veuillez vérifier que Docker Desktop est bien ouvert et que la virtualisation est active.")
    return False

def run_docker_compose(docker_bin):
    """Exécute docker compose up --build -d dans le dossier du projet."""
    print("\n📦 Construction et démarrage des conteneurs BAOU Finance (Backend & Admin)...")
    cmd = [docker_bin, "compose", "up", "--build", "-d"]
    
    try:
        res = subprocess.run(cmd, cwd=str(PROJECT_DIR), text=True)
        if res.returncode == 0:
            print("\n🎉 Projet BAOU Finance démarré avec succès !")
            print("--------------------------------------------------")
            print("🌐 Panneau Admin Web : http://localhost:3000")
            print("⚡ Backend API Express : http://localhost:3001")
            print("--------------------------------------------------")
            
            # Ouverture automatique du navigateur
            print("🌐 Ouverture du Panneau Admin dans votre navigateur...")
            time.sleep(2)
            webbrowser.open("http://localhost:3000")
            return True
        else:
            print(f"❌ Échec de la commande docker compose (code retour: {res.returncode})")
            return False
    except Exception as e:
        print(f"❌ Erreur lors de l'exécution de docker compose : {e}")
        return False

def show_menu(docker_bin):
    """Menu interactif de gestion des conteneurs."""
    while True:
        print("\n--- 🛠️ MENU BAOU FINANCE DOCKER ---")
        print("1. Voir les logs en direct (docker compose logs -f)")
        print("2. Voir l'état des conteneurs (docker compose ps)")
        print("3. Redémarrer les serveurs (docker compose restart)")
        print("4. Arrêter les serveurs Docker (docker compose down)")
        print("5. Ouvrir le panneau Admin Web dans le navigateur")
        print("6. Quitter")
        
        choice = input("Faites votre choix (1-6) : ").strip()
        
        if choice == "1":
            try:
                subprocess.run([docker_bin, "compose", "logs", "-f"], cwd=str(PROJECT_DIR))
            except KeyboardInterrupt:
                pass
        elif choice == "2":
            subprocess.run([docker_bin, "compose", "ps"], cwd=str(PROJECT_DIR))
        elif choice == "3":
            subprocess.run([docker_bin, "compose", "restart"], cwd=str(PROJECT_DIR))
            print("✅ Serveurs redémarrés !")
        elif choice == "4":
            subprocess.run([docker_bin, "compose", "down"], cwd=str(PROJECT_DIR))
            print("🛑 Serveurs Docker arrêtés.")
        elif choice == "5":
            webbrowser.open("http://localhost:3000")
        elif choice == "6":
            print("👋 Au revoir !")
            break
        else:
            print("Option invalide.")

def main():
    print("=" * 60)
    print("   🐘 BAOU FINANCE v2.0 — Lanceur Automatique Docker   ")
    print("=" * 60)
    print(f"📁 Dossier du projet : {PROJECT_DIR}\n")

    docker_bin = find_docker_bin()
    print(f"🔍 Executable Docker détecté : {docker_bin}")

    if not is_docker_engine_running(docker_bin):
        print("💡 Docker Engine n'est pas encore prêt.")
        start_docker_desktop()
        ready = wait_for_docker_engine(docker_bin)
        if not ready:
            sys.exit(1)
    else:
        print("✅ Docker Engine est déjà en cours d'exécution.")

    success = run_docker_compose(docker_bin)
    if success:
        show_menu(docker_bin)

if __name__ == "__main__":
    main()
