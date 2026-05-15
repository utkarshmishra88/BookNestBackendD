$services = "api-gateway", "auth-service", "book-service", "cart-service", "eureka-server", "notification-service", "order-service", "payment-service", "review-service", "wallet-service", "wishlist-service"

foreach ($service in $services) {
    Write-Host "----------------------------------------------------"
    Write-Host "Processing $service..."
    Write-Host "----------------------------------------------------"
    
    if (Test-Path "$service\mvnw.cmd") {
        Push-Location $service
        Write-Host "Running tests..."
        .\mvnw.cmd clean test
        
        Write-Host "Running Sonar scan..."
        .\mvnw.cmd sonar:sonar "-Dsonar.host.url=http://localhost:9001" "-Dsonar.login=admin" "-Dsonar.password=admin123"
        Pop-Location
    } else {
        Write-Host "Skipping $service - mvnw.cmd not found."
    }
}
