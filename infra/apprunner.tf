data "aws_iam_policy_document" "apprunner_ecr_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["build.apprunner.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "apprunner_ecr_access" {
  count = var.deploy_service ? 1 : 0

  name               = "${var.project}-ecr-access"
  assume_role_policy = data.aws_iam_policy_document.apprunner_ecr_assume_role.json
}

resource "aws_iam_role_policy_attachment" "apprunner_ecr_access" {
  count = var.deploy_service ? 1 : 0

  role       = aws_iam_role.apprunner_ecr_access[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

resource "aws_apprunner_service" "app" {
  count = var.deploy_service ? 1 : 0

  service_name = var.project

  source_configuration {
    auto_deployments_enabled = false

    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_access[0].arn
    }

    image_repository {
      image_identifier      = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
      image_repository_type = "ECR"

      image_configuration {
        port = "8080"

        runtime_environment_variables = {
          AWS_REGION          = var.region
          DYNAMODB_TABLE_NAME = aws_dynamodb_table.franchises.name
        }
      }
    }
  }

  instance_configuration {
    cpu               = var.service_cpu
    memory            = var.service_memory
    instance_role_arn = aws_iam_role.app.arn
  }

  health_check_configuration {
    protocol            = "HTTP"
    path                = "/actuator/health/readiness"
    interval            = 10
    timeout             = 5
    healthy_threshold   = 1
    unhealthy_threshold = 5
  }
}
