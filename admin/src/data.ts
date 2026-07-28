import { Transaction, User, SupportTicket, MarketQuote } from './types';

export const initialTransactions: Transaction[] = [];

export const initialUsers: User[] = [];

export const initialTickets: SupportTicket[] = [];

export const initialMarketQuotes: MarketQuote[] = [
  {
    ticker: 'SNTS',
    name: 'Sonatel CI',
    price: 16200,
    change: 1.2,
    volume: 124500000,
    high: 16400,
    low: 16100,
    sparkline: [16100, 16150, 16120, 16250, 16300, 16200, 16180, 16220, 16200]
  },
  {
    ticker: 'ONTBF',
    name: 'Onatel BF',
    price: 5000,
    change: -0.8,
    volume: 48200000,
    high: 5120,
    low: 4950,
    sparkline: [5120, 5080, 5050, 4980, 4950, 4990, 5000, 4980, 5000]
  },
  {
    ticker: 'CABC',
    name: 'SGB CI',
    price: 10000,
    change: 2.4,
    volume: 185000000,
    high: 10200,
    low: 9750,
    sparkline: [9750, 9800, 9900, 10000, 10100, 9950, 10050, 10150, 10000]
  },
  {
    ticker: 'BICI',
    name: 'BICICI',
    price: 7500,
    change: 0.5,
    volume: 32000000,
    high: 7600,
    low: 7450,
    sparkline: [7450, 7480, 7500, 7520, 7490, 7510, 7530, 7500, 7500]
  },
  {
    ticker: 'ETI',
    name: 'Ecobank TI',
    price: 200,
    change: -1.5,
    volume: 15400000,
    high: 210,
    low: 195,
    sparkline: [210, 205, 200, 198, 195, 198, 202, 201, 200]
  },
  {
    ticker: 'BOAS',
    name: 'Bank Of Africa SN',
    price: 3200,
    change: 3.1,
    volume: 59000000,
    high: 3250,
    low: 3100,
    sparkline: [3100, 3120, 3150, 3180, 3200, 3170, 3210, 3230, 3200]
  }
];
