Write-Host "Stopping all backend microservices..."
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Write-Host "All microservices stopped."
