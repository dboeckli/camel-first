cd target/helm/repo

$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
Write-Host "test application: $APPLICATION_NAME"
helm test $APPLICATION_NAME --namespace camel-first --logs
