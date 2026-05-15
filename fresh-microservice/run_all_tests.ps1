$mvn = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.4\plugins\maven\lib\maven3\bin\mvn.cmd"
$services = "api-gateway", "auth-service", "book-service", "cart-service", "eureka-server", "notification-service", "order-service", "payment-service", "review-service", "wallet-service", "wishlist-service"

foreach ($service in $services) {
    Write-Host "Running tests for $service..."
    Push-Location $service
    & $mvn clean test jacoco:report
    Pop-Location
}
Write-Host "All tests completed!"
