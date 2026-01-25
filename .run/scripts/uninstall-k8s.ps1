cd target/helm/repo

$file = Get-ChildItem -Filter camel-first-v*.tgz | Select-Object -First 1
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name

helm uninstall $APPLICATION_NAME --namespace camel-first

kubectl delete pod -n camel-first --field-selector=status.phase==Succeeded
kubectl delete pod -n camel-first --field-selector=status.phase==Failed
