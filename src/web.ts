import { WebPlugin } from '@capacitor/core';

import type { VNPayIntegratedPlugin } from './definitions';

export class VNPayIntegratedWeb
  extends WebPlugin
  implements VNPayIntegratedPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
