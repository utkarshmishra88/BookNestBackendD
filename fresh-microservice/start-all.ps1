$services = @(
    "eureka-server",
    "api-gateway",
    "auth-service",
    "book-service",
    "cart-service",
    "notification-service",
    "order-service",
    "payment-service",
    "review-service",
    "wallet-service",
    "wishlist-service"
)

Write-Host "Killing any existing java processes..."
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

foreach ($service in $services) {
    Write-Host "Starting $service in a new window..."
    
    # We set MAVEN_OPTS to restrict memory, then run mvnw in a NORMAL visible window
    $command = "cd $service; `$env:MAVEN_OPTS='-Xmx256m -Xms128m'; .\mvnw.cmd spring-boot:run"
    
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit -Command `"$command`"" -WindowStyle Normal
    
    # Wait a few seconds before starting the next one
    Start-Sleep -Seconds 5
}

Write-Host ""
Write-Host "======================================================="
Write-Host "All 11 microservices have been started in separate visible windows!"
Write-Host "======================================================="
