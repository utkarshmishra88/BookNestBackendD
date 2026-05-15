$mvn = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.4\plugins\maven\lib\maven3\bin\mvn.cmd"
$services = "api-gateway", "auth-service", "book-service", "cart-service", "eureka-server", "notification-service", "order-service", "payment-service", "review-service", "wallet-service", "wishlist-service"

foreach ($service in $services) {
    Write-Host "Running Sonar analysis for $service..."
    if (Test-Path "$service\target\jacoco.exec") {
        Push-Location $service
        & $mvn sonar:sonar "-Dsonar.host.url=http://localhost:9001" "-Dsonar.token=sqa_f14fcd10ee8b1b4a642348429df1ef75c3bb71ba"
        Pop-Location
    } else {
        Write-Host "Skipping $service as no jacoco.exec found (tests might not have run yet)."
    }
}
