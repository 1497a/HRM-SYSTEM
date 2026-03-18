$ErrorActionPreference = 'Stop'

$workspace = Split-Path -Parent $PSScriptRoot
$mainOut = Join-Path $workspace 'bin'
$testOut = Join-Path $workspace 'bin-test'
$repoRoot = Join-Path $env:USERPROFILE '.m2\repository'

$junitApi = Join-Path $repoRoot 'org\junit\jupiter\junit-jupiter-api\5.10.2\junit-jupiter-api-5.10.2.jar'
$openTest = Join-Path $repoRoot 'org\opentest4j\opentest4j\1.3.0\opentest4j-1.3.0.jar'
$apiGuardian = Join-Path $repoRoot 'org\apiguardian\apiguardian-api\1.1.2\apiguardian-api-1.1.2.jar'
$platformCommons = Join-Path $repoRoot 'org\junit\platform\junit-platform-commons\1.10.2\junit-platform-commons-1.10.2.jar'

$requiredJars = @($junitApi, $openTest, $apiGuardian, $platformCommons)
$missing = $requiredJars | Where-Object { -not (Test-Path $_) }
if ($missing.Count -gt 0) {
    Write-Error ("Missing JUnit jars:`n" + ($missing -join "`n"))
}

if (-not (Test-Path $mainOut)) {
    New-Item -ItemType Directory -Force -Path $mainOut | Out-Null
}
Copy-Item (Join-Path $workspace 'src\main\resources\*') $mainOut -Recurse -Force
if (Test-Path $testOut) {
    Remove-Item -Recurse -Force $testOut
}
New-Item -ItemType Directory -Force -Path $testOut | Out-Null

$testSources = Get-ChildItem (Join-Path $workspace 'src\test\java') -Recurse -Filter '*Test.java' |
    Select-Object -ExpandProperty FullName
$runnerSource = Join-Path $workspace 'src\test\java\com\hrm\testutil\SimpleTestRunner.java'

$testClasspath = @(
    $mainOut
    $junitApi
    $openTest
    $apiGuardian
    $platformCommons
) -join ';'

& javac -encoding UTF-8 -cp $testClasspath -d $testOut @($testSources + $runnerSource)
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$testClasses = Get-ChildItem $testOut -Recurse -Filter '*Test.class' |
    ForEach-Object {
        $_.FullName.Substring($testOut.Length + 1).Replace('\', '.').Replace('/', '.').Replace('.class', '')
    } |
    Sort-Object

$runtimeClasspath = @(
    $workspace
    $mainOut
    $testOut
    $junitApi
    $openTest
    $apiGuardian
    $platformCommons
) -join ';'

& java -cp $runtimeClasspath com.hrm.testutil.SimpleTestRunner @testClasses
exit $LASTEXITCODE
