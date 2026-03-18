$ErrorActionPreference = 'Stop'

$workspace = Split-Path -Parent $PSScriptRoot
$docPath = Join-Path $workspace 'docs\test-scenarios.md'
$sqlPath = Join-Path $workspace 'sql\hrm_smaple_data.sql'

if (-not (Test-Path $docPath)) {
    throw "Missing doc file: $docPath"
}
if (-not (Test-Path $sqlPath)) {
    throw "Missing SQL file: $sqlPath"
}

$docLines = Get-Content $docPath
$sqlText = Get-Content $sqlPath -Raw

$usernames = @()
foreach ($line in $docLines) {
    if ($line -match '^\|\s*([a-zA-Z0-9._-]+)\s*\|') {
        $candidate = $matches[1]
        if ($candidate -ne 'Username' -and $candidate -ne '---') {
            $usernames += $candidate
        }
    }
}
$usernames = $usernames | Select-Object -Unique

$report = foreach ($username in $usernames) {
    $found = $sqlText -match [regex]::Escape("('$username'")
    [pscustomobject]@{
        Username = $username
        PresentInSql = $found
    }
}

$missing = $report | Where-Object { -not $_.PresentInSql }

Write-Output "Audit file: $docPath"
Write-Output "Against SQL: $sqlPath"
Write-Output ""
Write-Output "Accounts in docs: $($report.Count)"
Write-Output "Missing in SQL : $($missing.Count)"
Write-Output ""
$report | Format-Table -AutoSize

if ($missing.Count -gt 0) {
    Write-Output ""
    Write-Output "Missing accounts:"
    $missing | ForEach-Object { Write-Output "- $($_.Username)" }
    exit 1
}
