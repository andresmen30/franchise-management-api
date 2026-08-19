output "table_name" {
  description = "Nombre de la tabla. Se pasa a la aplicacion como DYNAMODB_TABLE_NAME."
  value       = aws_dynamodb_table.franchises.name
}

output "table_arn" {
  description = "ARN de la tabla, al que queda restringido el rol de la aplicacion."
  value       = aws_dynamodb_table.franchises.arn
}

output "ecr_repository_url" {
  description = "Destino al que se publica la imagen de la aplicacion."
  value       = aws_ecr_repository.app.repository_url
}

output "app_role_arn" {
  description = "Rol que asume la aplicacion en ejecucion."
  value       = aws_iam_role.app.arn
}
