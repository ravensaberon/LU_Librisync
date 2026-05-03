param(
    [string]$DbUrl = "jdbc:mysql://127.0.0.1:3306/lu_librisync?useSSL=false&serverTimezone=Asia/Manila&allowPublicKeyRetrieval=true",
    [string]$DbUsername = "root",
    [string]$DbPassword,
    [switch]$SkipPasswordPrompt
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$mavenCandidates = @(
    (Join-Path $projectRoot "tools\apache-maven-3.9.14\bin\mvn.cmd"),
    "C:\Users\labar\Downloads\apache-maven-3.9.9\bin\mvn.cmd"
)
$systemMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($systemMaven) {
    $mavenCandidates += $systemMaven.Source
}
$mavenCmd = $mavenCandidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
$mysqlCli = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$mavenRepo = Join-Path $projectRoot ".m2\repository"
$fallbackJavaHome = Join-Path $env:USERPROFILE ".jdks\temurin-17"
$localEnvFile = Join-Path $PSScriptRoot "local-dev-env.env"

if (-not $mavenCmd) {
    throw "Maven was not found in the project tools folder, Downloads fallback, or system PATH."
}

New-Item -ItemType Directory -Force -Path $mavenRepo | Out-Null

if (Test-Path (Join-Path $fallbackJavaHome "bin\java.exe")) {
    $env:JAVA_HOME = $fallbackJavaHome
    if (-not (($env:Path -split ';') -contains (Join-Path $fallbackJavaHome "bin"))) {
        $env:Path = (Join-Path $fallbackJavaHome "bin") + ";" + $env:Path
    }
}

if (Test-Path $localEnvFile) {
    Get-Content $localEnvFile | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $separatorIndex = $line.IndexOf("=")
        if ($separatorIndex -lt 1) {
            return
        }

        $name = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1).Trim()
        Set-Item -Path ("Env:" + $name) -Value $value
    }
}

if (-not $PSBoundParameters.ContainsKey("DbPassword") -and -not $SkipPasswordPrompt) {
    $securePassword = Read-Host "Enter MySQL password for user '$DbUsername' (press Enter if none)" -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
}

$env:LU_LIBRISYNC_DB_URL = $DbUrl
$env:LU_LIBRISYNC_DB_USERNAME = $DbUsername
$env:LU_LIBRISYNC_DB_PASSWORD = $DbPassword

Write-Host ""
Write-Host "LU Librisync local run configuration" -ForegroundColor Cyan
Write-Host "DB URL     : $DbUrl"
Write-Host "DB Username: $DbUsername"

if (Test-Path $mysqlCli) {
    Write-Host "Checking MySQL access..." -ForegroundColor DarkCyan
    $mysqlArgs = @("-u", $DbUsername, "-e", "USE lu_librisync; SELECT 'Database connection OK' AS status;")
    if (-not [string]::IsNullOrEmpty($DbPassword)) {
        $mysqlArgs = @("-u", $DbUsername, "-p$DbPassword", "-e", "USE lu_librisync; SELECT 'Database connection OK' AS status;")
    }

    & $mysqlCli @mysqlArgs 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "MySQL connection test failed. The app may still start, but please verify your username/password and that the 'lu_librisync' database exists."
    }
}

Push-Location $projectRoot
try {
    & $mavenCmd "-o" "-Dmaven.repo.local=$mavenRepo" spring-boot:run
} finally {
    Pop-Location
}
