variable "project" {
  description = "Nombre del proyecto, usado como prefijo de los recursos y como etiqueta."
  type        = string
  default     = "franchise-management-api"
}

variable "region" {
  description = "Region de AWS donde se crean los recursos."
  type        = string
  default     = "us-east-1"
}

variable "table_name" {
  description = "Nombre de la tabla de DynamoDB que respalda el dominio completo."
  type        = string
  default     = "franchises"
}

variable "point_in_time_recovery" {
  description = "Habilita la recuperacion a un punto en el tiempo. Tiene costo adicional por almacenamiento."
  type        = bool
  default     = false
}

variable "image_versions_to_keep" {
  description = "Cantidad de imagenes recientes que conserva el repositorio de ECR."
  type        = number
  default     = 5
}

variable "deploy_service" {
  description = "Crea el servicio de App Runner. Apagado por defecto: no tiene capa gratuita y cobra la memoria aprovisionada aunque este ocioso."
  type        = bool
  default     = false
}

variable "image_tag" {
  description = "Etiqueta de la imagen en ECR que ejecuta el servicio."
  type        = string
  default     = "latest"
}

variable "service_cpu" {
  description = "vCPU asignadas al servicio, en unidades de App Runner."
  type        = string
  default     = "1024"
}

variable "service_memory" {
  description = "Memoria asignada al servicio, en MB. Es lo que se cobra aunque este ocioso."
  type        = string
  default     = "2048"
}
