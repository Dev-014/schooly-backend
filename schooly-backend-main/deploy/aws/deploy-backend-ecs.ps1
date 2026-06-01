$ErrorActionPreference = "Stop"
# Let AWS CLI non-zero exits be handled via $LASTEXITCODE checks below.
$PSNativeCommandUseErrorActionPreference = $false

# ----------------------------
# REQUIRED INPUTS
# ----------------------------
$Region = (aws configure get region).Trim()
if ([string]::IsNullOrWhiteSpace($Region)) {
  $Region = "us-east-1"
}
$AccountId = "057917758891"
$RepositoryName = "school-erp-backend"
$ImageTag = "v1"
$ClusterName = "school-erp-cluster"
$ServiceName = "school-erp-backend-service-v2"
$TaskFamily = "school-erp-backend-task"
$ContainerName = "school-erp-backend"
$Subnets = "subnet-0cde0b5f2bed8ccfc,subnet-02240f3f82b729229"
$SecurityGroups = "sg-062001a3e2d20085f"
$TargetGroupArn = "arn:aws:elasticloadbalancing:us-east-1:057917758891:targetgroup/school-erp-tg/a687c6d341b1715d"
$EcsTaskExecutionRoleArn = "arn:aws:iam::057917758891:role/ecsTaskExecutionRole"
$EcsTaskRoleArn = "arn:aws:iam::057917758891:role/schoolErpTaskRole"
$DbJdbcUrl = "jdbc:postgresql://school-erp-postgres.cb408e4qu5zy.eu-north-1.rds.amazonaws.com:5432/school_erp"
$DbUsername = "school_erp_app"
$DbPasswordSecretArn = "arn:aws:secretsmanager:us-east-1:057917758891:secret:school-erp/db-password-l9DO4s"
$JwtSecretArn = "arn:aws:secretsmanager:us-east-1:057917758891:secret:school-erp/jwt-secret"
$ImageUri = "$AccountId.dkr.ecr.$Region.amazonaws.com/${RepositoryName}:${ImageTag}"

Write-Host "Ensuring ECR repository exists..."
$PreviousEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
aws ecr describe-repositories --region $Region --repository-names $RepositoryName 2>$null | Out-Null
$ErrorActionPreference = $PreviousEap
if ($LASTEXITCODE -ne 0) {
  Write-Host "ECR repository not found (or describe denied). Attempting to create..."
  aws ecr create-repository --region $Region --repository-name $RepositoryName | Out-Null
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to describe/create ECR repository. Verify IAM permissions for ecr:DescribeRepositories and ecr:CreateRepository in region $Region."
  }
}

Write-Host "Logging in to ECR..."
aws ecr get-login-password --region $Region | docker login --username AWS --password-stdin "$AccountId.dkr.ecr.$Region.amazonaws.com"

Write-Host "Building and pushing Docker image: $ImageUri ..."
docker build -t "${RepositoryName}:${ImageTag}" .
docker tag "${RepositoryName}:${ImageTag}" $ImageUri
docker push $ImageUri

Write-Host "Preparing task definition from template..."
$TemplatePath = "deploy/aws/ecs-taskdef.template.json"
$TaskDefPath = "deploy/aws/ecs-taskdef.generated.json"
$TaskDef = Get-Content $TemplatePath -Raw
$TaskDef = $TaskDef.Replace("__ECS_TASK_EXECUTION_ROLE_ARN__", $EcsTaskExecutionRoleArn)
$TaskDef = $TaskDef.Replace("__ECS_TASK_ROLE_ARN__", $EcsTaskRoleArn)
$TaskDef = $TaskDef.Replace("__ECR_IMAGE_URI__", $ImageUri)
$TaskDef = $TaskDef.Replace("__SPRING_DATASOURCE_URL__", $DbJdbcUrl)
$TaskDef = $TaskDef.Replace("__SPRING_DATASOURCE_USERNAME__", $DbUsername)
$TaskDef = $TaskDef.Replace("__SECRETS_MANAGER_PASSWORD_ARN__", $DbPasswordSecretArn)
$TaskDef = $TaskDef.Replace("__SECRETS_MANAGER_JWT_SECRET_ARN__", $JwtSecretArn)
$TaskDef = $TaskDef.Replace("__AWS_REGION__", $Region)
$TaskDef | Set-Content $TaskDefPath

Write-Host "Registering ECS task definition..."
$TaskDefArn = aws ecs register-task-definition `
  --region $Region `
  --cli-input-json file://$TaskDefPath `
  --query "taskDefinition.taskDefinitionArn" `
  --output text

Write-Host "Ensuring ECS cluster exists..."
$PreviousEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$ClusterStatus = aws ecs describe-clusters --region $Region --clusters $ClusterName --query "clusters[0].status" --output text 2>$null
$ErrorActionPreference = $PreviousEap
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($ClusterStatus) -or $ClusterStatus -eq "None") {
  Write-Host "Cluster not found. Creating ECS cluster: $ClusterName ..."
  aws ecs create-cluster --region $Region --cluster-name $ClusterName | Out-Null
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to create ECS cluster $ClusterName in region $Region."
  }
}

Write-Host "Checking if ECS service exists..."
$PreviousEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$ServiceStatus = aws ecs describe-services --region $Region --cluster $ClusterName --services $ServiceName --query "services[0].status" --output text 2>$null
$ErrorActionPreference = $PreviousEap
if ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace($ServiceStatus) -and $ServiceStatus -ne "None") {
  Write-Host "Updating existing ECS service..."
  aws ecs update-service `
    --region $Region `
    --cluster $ClusterName `
    --service $ServiceName `
    --task-definition $TaskDefArn `
    --health-check-grace-period-seconds 180 `
    --force-new-deployment | Out-Null
} else {
  Write-Host "Creating ECS service..."
  aws ecs create-service `
    --region $Region `
    --cluster $ClusterName `
    --service-name $ServiceName `
    --task-definition $TaskDefArn `
    --desired-count 1 `
    --health-check-grace-period-seconds 180 `
    --launch-type FARGATE `
    --network-configuration "awsvpcConfiguration={subnets=[$Subnets],securityGroups=[$SecurityGroups],assignPublicIp=ENABLED}" `
    --load-balancers "targetGroupArn=$TargetGroupArn,containerName=$ContainerName,containerPort=8080" | Out-Null
}

Write-Host "Done. ECS deployment triggered."
