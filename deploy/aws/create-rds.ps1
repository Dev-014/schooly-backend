$ErrorActionPreference = "Stop"

# ----------------------------
# REQUIRED INPUTS
# ----------------------------
$Region = (aws configure get region).Trim()
if ([string]::IsNullOrWhiteSpace($Region)) {
  $Region = "us-east-1"
}
$DbIdentifier = "school-erp-postgres"
$DbName = "school_erp"
$DbUsername = "school_erp_app"
$DbPassword = "StrongPassword123"
$DbInstanceClass = "db.t3.micro"
$DbAllocatedStorage = 30
$VpcSecurityGroupId = "sg-0c7acf7e9327a5417"
$DbSubnetGroupName = "school-erp-subnet-group"

# Optional production toggles
$MultiAz = $false
$PubliclyAccessible = $true

$MultiAzArg = if ($MultiAz) { "--multi-az" } else { "--no-multi-az" }
$PubliclyAccessibleArg = if ($PubliclyAccessible) { "--publicly-accessible" } else { "--no-publicly-accessible" }

Write-Host "Creating RDS PostgreSQL instance: $DbIdentifier in $Region ..."

aws rds create-db-instance `
  --region $Region `
  --db-instance-identifier $DbIdentifier `
  --db-instance-class $DbInstanceClass `
  --engine postgres `
  --allocated-storage $DbAllocatedStorage `
  --master-username $DbUsername `
  --master-user-password $DbPassword `
  --db-name $DbName `
  --vpc-security-group-ids $VpcSecurityGroupId `
  --db-subnet-group-name $DbSubnetGroupName `
  --no-deletion-protection `
  --backup-retention-period 1 `
  --storage-encrypted `
  $MultiAzArg `
  $PubliclyAccessibleArg

Write-Host "Waiting for DB to become available..."
aws rds wait db-instance-available --region $Region --db-instance-identifier $DbIdentifier

$Endpoint = aws rds describe-db-instances `
  --region $Region `
  --db-instance-identifier $DbIdentifier `
  --query "DBInstances[0].Endpoint.Address" `
  --output text

Write-Host "RDS endpoint: $Endpoint"
Write-Host "JDBC URL: jdbc:postgresql://$Endpoint`:5432/$DbName"
