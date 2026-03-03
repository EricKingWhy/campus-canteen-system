
# Start Chrome with Remote Debugging Port 9222
# This allows the Chrome DevTools MCP Server to connect to it.

$chromePath = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$port = 9222
$userDataDir = "$env:TEMP\chrome_debug_profile"

if (-not (Test-Path $chromePath)) {
    $chromePath = "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
}

if (-not (Test-Path $chromePath)) {
    Write-Host "Chrome not found at standard locations." -ForegroundColor Red
    exit 1
}

Write-Host "Starting Chrome with --remote-debugging-port=$port" -ForegroundColor Green
Write-Host "Using temporary user data dir: $userDataDir" -ForegroundColor Yellow

# Start Chrome detached
Start-Process -FilePath $chromePath -ArgumentList "--remote-debugging-port=$port", "--user-data-dir=$userDataDir", "--no-first-run", "--no-default-browser-check"

Write-Host "Done. You can now use Chrome DevTools MCP." -ForegroundColor Green
