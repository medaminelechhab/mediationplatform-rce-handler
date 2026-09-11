{{- define "helm.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "helm.labelsMediation" -}}
helm.sh/chart: {{ include "helm.chart" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
meta.helm.sh/release-name: {{  .Release.Name }}
meta.helm.sh/release-namespace : {{ .Values.namespace }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}