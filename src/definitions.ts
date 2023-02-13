export interface VNPayIntegratedPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
