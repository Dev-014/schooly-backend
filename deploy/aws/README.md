# AWS Deployment (Database + Backend)

This folder contains a practical deployment path for:
- PostgreSQL on **RDS**
- Spring Boot backend on **ECS Fargate** behind ALB

## 1) What is already prepared in code

- Container build file: `Dockerfile`
- Environment-driven DB config in `src/main/resources/application.properties`
- ECS task definition template: `deploy/aws/ecs-taskdef.template.json`
- Script to create RDS: `deploy/aws/create-rds.ps1`
- Script to build/push/deploy backend to ECS: `deploy/aws/deploy-backend-ecs.ps1`

## 2) What you must do first (one-time AWS setup)

1. Install tools locally:
   - AWS CLI v2
   - Docker Desktop
2. Configure AWS credentials:
   - `aws configure`
3. Create networking:
   - VPC, private subnets (at least 2 AZ), route tables, NAT
4. Create security groups:
   - `rds-sg`: allow `5432` only from ECS task SG
   - `ecs-sg`: allow `8080` from ALB SG
   - `alb-sg`: allow `80/443` from internet
5. Create DB subnet group for RDS (private subnets).
6. Create ALB + target group (`ip` type, port `8080`, health check path `/actuator/health` or `/api/dashboard/kpis?schoolId=1` if no actuator).
7. Create IAM roles:
   - `ecsTaskExecutionRole` (AWS managed policy: `AmazonECSTaskExecutionRolePolicy`)
   - Task role for app (`schoolErpTaskRole`) with permissions to read Secrets Manager.
8. Store DB password in Secrets Manager.

## 3) Deploy database (RDS)

1. Open `deploy/aws/create-rds.ps1`.
2. Fill required values:
   - `Region`
   - `VpcSecurityGroupId`
   - `DbSubnetGroupName`
   - DB credentials/size/version
3. Run:
   - `powershell -ExecutionPolicy Bypass -File deploy/aws/create-rds.ps1`
4. Copy output JDBC endpoint.

## 4) Deploy backend (ECS Fargate)

1. Open `deploy/aws/deploy-backend-ecs.ps1`.
2. Fill all required placeholders:
   - account, region, subnet IDs, SG IDs
   - target group ARN
   - execution/task role ARNs
   - DB JDBC URL and DB username
   - Secrets Manager ARN for DB password
3. Run from repo root:
   - `powershell -ExecutionPolicy Bypass -File deploy/aws/deploy-backend-ecs.ps1`

This script will:
- Ensure ECR repo exists
- Build Docker image
- Push image to ECR
- Generate ECS task def
- Register task definition
- Create or update ECS service

## 5) Post-deploy checklist

1. Confirm ECS task is `RUNNING`.
2. Confirm target group health checks are green.
3. Test API:
   - `GET http://<ALB-DNS>/api/dashboard/kpis?schoolId=1`
4. Run DB migration SQL on RDS if not already applied.
5. Lock security groups to least privilege.

## 6) Recommended hardening for production

- Multi-AZ RDS
- Deletion protection for RDS
- CloudWatch alarms for ECS/RDS
- ALB HTTPS with ACM cert
- WAF on ALB (optional)
- CI/CD pipeline (GitHub Actions) for image build + deploy

