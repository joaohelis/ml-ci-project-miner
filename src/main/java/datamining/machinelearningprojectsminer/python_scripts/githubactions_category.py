

import requests

# Define as informações de autenticação da API do Github
headers = {
    "Authorization": "Token <seu token de acesso pessoal do Github>",
    "Accept": "application/vnd.github.v3+json"
}

# Define o nome do repositório a ser verificado
repo_name = "rails/rails"

# Faz uma requisição para obter a lista de workflows do repositório
url = f"https://api.github.com/repos/{repo_name}/actions/workflows"
response = requests.get(url, headers=headers)
response.raise_for_status()

# Analisa a resposta JSON e obtém a lista de fluxos de trabalho
workflows = response.json()["workflows"]

# Para cada fluxo de trabalho, faz uma nova requisição para obter as categorias de ações
for workflow in workflows:
    workflow_id = workflow["id"]
    url = f"https://api.github.com/repos/{repo_name}/actions/workflows/{workflow_id}"
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    
    # Analisa a resposta JSON e obtém as categorias de ações
    categories = response.json()["workflow"]["jobs"].values()
    
    # Imprime o nome do fluxo de trabalho e suas categorias de ações
    print(f"Workflow: {workflow['name']}")
    print("Categorias de ações:")
    for category in categories:
        print(category["name"])
    print("\n")