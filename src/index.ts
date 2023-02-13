import { registerPlugin } from '@capacitor/core';

import type { VNPayIntegratedPlugin } from './definitions';

const VNPayIntegrated = registerPlugin<VNPayIntegratedPlugin>(
  'VNPayIntegrated',
  {
    web: () => import('./web').then(m => new m.VNPayIntegratedWeb()),
  },
);

export * from './definitions';
export { VNPayIntegrated };
