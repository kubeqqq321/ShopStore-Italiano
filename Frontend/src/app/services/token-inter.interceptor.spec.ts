import { TestBed } from '@angular/core/testing';

import { TokenInterInterceptor } from './token-inter.interceptor';

describe('TokenInterInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      TokenInterInterceptor
      ]
  }));

  it('should be created', () => {
    const interceptor: TokenInterInterceptor = TestBed.inject(TokenInterInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
