# ml-ci-project-miner

## Overview

Welcome to **ml-ci-project-miner**, a Java-based tool designed for mining data from both machine learning (ML) and non-ML projects on GitHub. This project supports a comprehensive study outlined in the paper "HOW DO MACHINE LEARNING PROJECTS USE CONTINUOUS INTEGRATION PRACTICES? AN EMPIRICAL STUDY ON GITHUB ACTIONS," published in the proceedings of the MSR conference in 2024. For detailed results and access to the replication package, please visit the project's web page: [CI-ML-MSR](https://ci-ml-msr.github.io).

## Usage

### Database Configuration

Configure your MySQL database credentials in the "resources/hibernate.cfg.xml" file.

### APIs Configuration

Configure the class "config/Config.java" with your TOKENS for the APIs used in the project (i.e., GitHub API token).

### Project Initialization

Import the initial project list from the paper "Characterizing the usage of CI tools in ML projects" using the classes within the "importer" package.

### Data Retrieval

Retrieve data from project repositories using classes from the "miner" package. This involves leveraging APIs such as GitHub, Coveralls, CodeCov, and CodeTabs. To get started, explore the "GHAPIRepositoryMiner.java" class.

### Data Models

Explore the "models" package, which contains classes representing the data models for this project, including repository details, workflows, runs, etc.

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/joaohelis/ml-ci-project-miner.git

### Contribution

Feel free to contribute to this project by submitting issues, feature requests, or pull requests. Your input is valuable in enhancing the tool and its capabilities.

### License

This project is licensed under the MIT License.
