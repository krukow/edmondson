# Development and Testing Guide

This guide provides comprehensive information about setting up development environments, running tests, building the project, and using various development tools for Edmondson.

## Table of Contents

- [Overview](#overview)
- [Development Environment Options](#development-environment-options)
- [Prerequisites](#prerequisites)
- [Setting Up Development Environment](#setting-up-development-environment)
- [Testing](#testing)
- [Building](#building)
- [Docker Usage](#docker-usage)
- [Scripts and Utilities](#scripts-and-utilities)
- [Project Structure](#project-structure)
- [CI/CD](#cicd)

## Overview

Edmondson is a Clojure-based toolkit for analyzing survey data. The project supports multiple development environments and provides various tools for testing, building, and deployment.

## Development Environment Options

You can develop Edmondson using one of three approaches:

1. **Local development** - Install dependencies directly on your machine
2. **Docker-local** - Use Docker containers on your local machine
3. **GitHub Codespaces** - Use cloud-based development environment

Each approach has its own setup requirements and is suitable for different workflows.

## Prerequisites

### For Local Development

#### 1. Java/JDK
You must have Java/JDK installed (e.g., [adoptopenjdk.net](https://adoptopenjdk.net/)).

**Note**: If you are running macOS, you may already have Java installed.

On macOS:
```bash
export JAVA_HOME=`/usr/libexec/java_home`
export PATH=$JAVA_HOME/bin:$PATH
```

#### 2. Clojure CLI
Install [Clojure and Clojure CLI tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).

#### 3. Optional: Jupyter (for notebook support)
Install Jupyter if you plan to work with Jupyter notebooks. We recommend the [Anaconda distribution](https://www.anaconda.com/products/individual).

Verify installation:
```bash
jupyter lab
```

### For Docker-based Development

- Docker installed and running on your machine
- Docker image: `krukow/edmondson:v1.3.3-dev6` or build your own

### For GitHub Codespaces

- Access to GitHub Codespaces
- GitHub account with the repository forked

## Setting Up Development Environment

### Local Development Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/krukow/edmondson.git
   cd edmondson
   ```

2. **Set up API credentials**:
   - Follow instructions in the [main README](./README.md#4-api-access) to generate `credentials.json`
   - Run authentication:
     ```bash
     export GOOGLE_CREDENTIALS_JSON=`pwd`/client_secret_....json
     clj -X:google-oauth2
     ```
   - This stores tokens in `./tokens/StoredCredential`

3. **Install project dependencies** (automatically downloaded on first run):
   ```bash
   clj -P  # Pre-download dependencies
   ```

4. **Set up Jupyter kernel** (if using Jupyter):
   ```bash
   ./script/go.sh
   ```

### Docker-local Development Setup

1. **Run Docker container**:
   ```bash
   docker run -it -u jovyan -p 8888:8888 -p 8889:8889 \
       -v ${PWD}:/home/jovyan/work/ \
       -w /home/jovyan/ \
       krukow/edmondson:v1.3.3-dev6 \
       /bin/bash
   ```

2. **Inside the container**:
   ```bash
   cd work/
   ./script/go.sh docker
   ```

3. **Set up credentials** (same as local):
   - Copy your `credentials.json` into the mounted volume
   - Copy `tokens/StoredCredential` if already authenticated

### GitHub Codespaces Setup

1. **Create Codespace**:
   - Fork the repository on GitHub
   - Click: Code > Codespaces > New codespace

2. **Build in Codespaces**:
   ```bash
   ./script/go.sh docker
   ```

3. **Upload credentials**:
   - Upload `client_secret_....json` to top-level directory
   - Upload `tokens/StoredCredential` to `tokens/` directory
   - Rename: `mv client_secret_....json ./credentials.json`

4. **Start Jupyter**:
   ```bash
   ./lab
   ```
   - Codespaces will forward port 8889
   - Click "Open in browser" and enter the token shown in the terminal

## Testing

### Running Tests

The project uses Cognitect test-runner for running Clojure tests.

**Run all tests**:
```bash
clojure -X:test
```

**Run tests with specific options**:
```bash
clojure -M:test
```

**Run tests for specific namespace**:
```bash
clojure -X:test :dirs '["src/test/clojure"]' :nses '[edmondson.config-test]'
```

### Test Structure

- Test files are located in: `src/test/clojure/`
- Test namespaces follow the pattern: `<namespace>-test`
- Tests use `clojure.test` framework

### Example Test Execution

```bash
# Simple test run
clojure -X:test

# Expected output:
# Running tests in #{"src/test/clojure"}
# 
# Testing edmondson.config-test
# 
# Ran 1 tests containing 7 assertions.
# 0 failures, 0 errors.
```

## Building

### Build Commands

The project uses Clojure's tools.build for creating artifacts.

**Build uberjar using tools.build** (recommended for local):
```bash
clojure -T:build uber
```

This creates: `target/io.github.krukow-edmondson-<version>-standalone.jar`

**Build uberjar using depstar** (used in Docker):
```bash
./script/build.sh docker
```

This creates: `target/Edmondson-standalone.jar`

**Clean build artifacts**:
```bash
clojure -T:build clean
# or
./script/clean.sh
```

### Build Script Details

The `script/build.sh` script:
- Supports both local and Docker builds
- Uses Maven options for retry and connection management
- Outputs the path to the built JAR file

## Docker Usage

### Building Docker Images

#### Base Container

1. **Set build name**:
   ```bash
   export BUILD=lab-3-4-5-clojupyter-0-3-5-base-1
   ```

2. **Build image**:
   ```bash
   mkdir -p tmp
   docker build -f .devcontainer/Dockerfile -t krukow/edmondson:$BUILD ./tmp
   ```

3. **Push to registry**:
   ```bash
   docker push krukow/edmondson:$BUILD
   ```

#### Development Container

1. **Run container from base image**:
   ```bash
   docker run -it -u jovyan -p 8888:8888 \
       -v ${PWD}:/home/jovyan/work/ \
       -w /home/jovyan/ \
       krukow/edmondson:$BUILD \
       /bin/bash
   ```

2. **Inside container, build project**:
   ```bash
   cd work/
   ./script/go.sh docker
   clojure -A:upload -P  # Download dependencies
   ```

3. **Commit container** (from host):
   ```bash
   dev_version="lab-3-4-5-clojupyter-0-3-5-dev-1"
   container_id=$(docker ps --format "{{.ID}}")
   docker commit --author "Your Name <your.email@example.com>" \
       -m "Dev container for $BUILD" \
       $container_id \
       krukow/edmondson:$dev_version
   ```

4. **Push dev image**:
   ```bash
   docker push krukow/edmondson:$dev_version
   ```

### Dockerfile Details

- **`.devcontainer/Dockerfile`**: Base image with Jupyter, Java, and Clojure
- **`Dockerfile-dev`**: Builds on base image and installs Edmondson kernel

## Scripts and Utilities

### Available Scripts

All scripts are located in the `script/` directory:

#### `go.sh`
Builds the uberjar and installs the Clojupyter kernel.

**Usage**:
```bash
./script/go.sh          # Local build
./script/go.sh docker   # Docker build
```

**What it does**:
- Calls `build.sh` to create uberjar
- Removes existing Clojupyter kernel installation
- Installs new kernel with the built JAR

#### `build.sh`
Builds the project uberjar.

**Usage**:
```bash
./script/build.sh        # Local build (uses tools.build)
./script/build.sh docker # Docker build (uses depstar)
```

**Output**: Path to the built JAR file

#### `analyze.sh`
Generates HTML reports from Jupyter notebooks.

**Usage**:
```bash
./script/analyze.sh <path-to-notebook.ipynb>
```

**Example**:
```bash
export NUM_PARTICIPANTS="39"
export TEAM_NAME="My team"
./script/analyze.sh examples/google_sheets/psych_safety_generative_culture.ipynb
```

**Output**: `results/html/report.html`

#### `analyze-actions-fast.sh`
Fast report generation for CI/CD environments.

**Usage**: Similar to `analyze.sh` but optimized for GitHub Actions.

#### `clean.sh`
Cleans build artifacts.

**Usage**:
```bash
./script/clean.sh
```

### Other Utilities

#### `lab`
Starts Jupyter Lab server.

**Usage**:
```bash
./lab
```

**What it does**:
- Trusts notebooks in `examples/google_sheets/`
- Starts Jupyter Lab on port 8889
- Binds to 0.0.0.0 for Codespaces compatibility

## Project Structure

```
edmondson/
├── .devcontainer/         # Codespaces/Docker configuration
│   ├── Dockerfile         # Base container definition
│   ├── Dockerfile-dev     # Dev container definition
│   └── devcontainer.json  # Codespaces settings
├── .github/
│   └── workflows/         # GitHub Actions workflows
├── config/                # Jupyter configuration
├── doc/                   # Documentation
│   ├── README.md         # Documentation index
│   ├── contributing.md   # Contributing guidelines
│   ├── dev.md           # Development notes
│   └── jupyter.md       # Jupyter setup guide
├── examples/             # Example notebooks and scripts
│   └── google_sheets/   # Google Sheets examples
├── script/              # Build and utility scripts
│   ├── analyze.sh       # Report generation
│   ├── build.sh        # Build script
│   ├── clean.sh        # Clean script
│   └── go.sh          # Build and install kernel
├── src/
│   ├── main/clojure/   # Main source code
│   │   └── edmondson/  # Core namespaces
│   └── test/clojure/   # Test files
│       └── edmondson/  # Test namespaces
├── templates/          # Report templates
├── tokens/            # OAuth tokens (gitignored)
├── build.clj          # Build configuration
├── deps.edn          # Dependencies and aliases
├── lab              # Jupyter Lab launcher script
├── LICENSE          # MIT License
└── README.md       # Main project README
```

### Key Files

- **`deps.edn`**: Clojure dependencies and aliases configuration
- **`build.clj`**: Build tool configuration (tools.build)
- **`.gitignore`**: Excludes credentials, tokens, build artifacts
- **`lab`**: Shell script to start Jupyter Lab

## CI/CD

### GitHub Actions

The project uses GitHub Actions for continuous integration:

- **CodeQL Analysis**: Security scanning workflow
- Located in: `.github/workflows/codeql-analysis.yml`
- Runs on: push to main, pull requests, scheduled weekly
- Language: Python (for Jupyter notebooks)

### Environment Variables

Common environment variables used in development and CI:

- **`GOOGLE_CREDENTIALS_JSON`**: Path to Google OAuth credentials file
- **`RESULTS_URL`**: Google Sheets URL for survey data (optional, defaults to example)
- **`TEAM_NAME`**: Team name for report customization (optional)
- **`NUM_PARTICIPANTS`**: Number of survey participants (optional)
- **`JAVA_HOME`**: Java installation directory
- **`MAVEN_OPTS`**: Maven JVM options (used in builds)

### Best Practices

1. **Never commit credentials**: Keep `credentials.json` and tokens out of version control
2. **Use environment variables**: For configuration that varies between environments
3. **Test before committing**: Run tests locally before pushing changes
4. **Keep builds reproducible**: Pin dependency versions in `deps.edn`
5. **Document changes**: Update relevant documentation when changing functionality

## IDE Integration

### Emacs with CIDER

Example supported environment (from `doc/contributing.md`):

- [Emacs Prelude](https://prelude.emacsredux.com/en/latest/) (includes CIDER and clojure-mode)
- Clojure CLI / tools.deps / [cider-nrepl](https://github.com/clojure-emacs/cider-nrepl)

**Start REPL**:
```elisp
;; In Emacs: C-u M-x cider-jack-in
;; Uses command:
/usr/local/bin/clojure -Sdeps '{:deps {nrepl {:mvn/version "0.8.3"} cider/cider-nrepl {:mvn/version "0.25.5"}}}' -m nrepl.cmdline --middleware '["cider.nrepl/cider-middleware"]'
```

### Other IDEs

The project uses standard Clojure tooling and should work with:
- IntelliJ IDEA + Cursive
- VS Code + Calva
- Vim/Neovim + vim-fireplace
- Any editor with nREPL support

## Running Examples

### REPL-based Example

```bash
# Set environment variables (optional)
export RESULTS_URL="<your-google-sheets-url>"

# Run example with REPL
clojure -A:examples -m google-sheets.psych-safety

# In the REPL, try:
# (report "Psychological safety")
```

### Jupyter Notebook Example

```bash
# Start Jupyter Lab
RESULTS_URL="..." TEAM_NAME="..." NUM_PARTICIPANTS="..." jupyter lab

# Open: examples/google_sheets/psych_safety_generative_culture.ipynb
# Select kernel: Clojure (edmondson)
# Run all cells
```

## Troubleshooting

### Common Issues

1. **"clojure: command not found"**
   - Solution: Install Clojure CLI tools
   - macOS: `brew install clojure/tools/clojure`
   - Linux: Follow instructions at https://clojure.org/guides/install_clojure

2. **"java: command not found"**
   - Solution: Install JDK and set JAVA_HOME

3. **OAuth/Authentication errors**
   - Solution: Regenerate credentials.json and run `clj -X:google-oauth2`

4. **Jupyter kernel not found**
   - Solution: Run `./script/go.sh` to install the kernel

5. **Build failures**
   - Check internet connection (Maven downloads dependencies)
   - Try cleaning: `./script/clean.sh`
   - Check Java version compatibility

6. **Docker port conflicts**
   - Change port mappings: `-p 8890:8888` instead of `-p 8888:8888`

## Getting Help

- **Documentation**: See [./doc/](./doc/) directory
- **Examples**: Check [./examples/](./examples/) directory
- **Issues**: Open an issue on GitHub
- **Contributing**: See [./doc/contributing.md](./doc/contributing.md)

## Additional Resources

- [Main README](./README.md) - Quick start guide
- [Jupyter Guide](./doc/jupyter.md) - Detailed Jupyter setup
- [Contributing Guide](./doc/contributing.md) - How to contribute
- [Clojure Documentation](https://clojure.org/) - Clojure language docs
- [Clojupyter](https://github.com/clojupyter/clojupyter) - Clojure Jupyter kernel
