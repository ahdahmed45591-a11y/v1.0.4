import React, { useState } from 'react';
import { MarketQuote } from '../types';
import { initialMarketQuotes } from '../data';
import { 
  Activity, 
  TrendingUp, 
  TrendingDown, 
  ArrowUpRight, 
  ArrowDownRight, 
  Percent, 
  BarChart4, 
  RefreshCw,
  Search,
  BookOpen
} from 'lucide-react';

export const MarketMonitoringView: React.FC = () => {
  const [quotes, setQuotes] = useState<MarketQuote[]>(initialMarketQuotes);
  const [selectedTicker, setSelectedTicker] = useState('SNTS');
  const [timeRange, setTimeRange] = useState<'1D' | '1W' | '1M' | '1Y'>('1M');
  const [searchTerm, setSearchTerm] = useState('');

  const currentQuote = quotes.find(q => q.ticker === selectedTicker) || quotes[0];

  // Simulated order book
  const orderBook = {
    buys: [
      { price: currentQuote.price * 0.995, qty: 1200, percent: 80 },
      { price: currentQuote.price * 0.992, qty: 3450, percent: 95 },
      { price: currentQuote.price * 0.990, qty: 850, percent: 45 },
      { price: currentQuote.price * 0.985, qty: 110500, percent: 100 }
    ],
    sells: [
      { price: currentQuote.price * 1.005, qty: 800, percent: 40 },
      { price: currentQuote.price * 1.008, qty: 2100, percent: 75 },
      { price: currentQuote.price * 1.010, qty: 5400, percent: 90 },
      { price: currentQuote.price * 1.015, qty: 12400, percent: 100 }
    ]
  };

  const handleRefresh = () => {
    // Simulate real-time ticks
    const updatedQuotes = quotes.map(q => {
      const multiplier = 1 + (Math.random() - 0.5) * 0.015; // Max 0.75% change
      const newPrice = Math.round(q.price * multiplier);
      const diff = newPrice - q.price;
      const pct = (diff / q.price) * 100;
      
      // Update sparkline
      const nextSpark = [...q.sparkline.slice(1), newPrice];

      return {
        ...q,
        price: newPrice,
        change: parseFloat((q.change + pct).toFixed(2)),
        high: Math.max(q.high, newPrice),
        low: Math.min(q.low, newPrice),
        sparkline: nextSpark
      };
    });
    setQuotes(updatedQuotes);
  };

  const formatAmount = (num: number) => {
    return new Intl.NumberFormat('fr-FR').format(num);
  };

  const filteredQuotes = quotes.filter(q => 
    q.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    q.ticker.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      
      {/* View Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="font-sans font-extrabold text-[24px] text-[#0b1c30] tracking-tight">
            Surveillance des Marchés
          </h2>
          <p className="font-sans text-[14px] text-[#574235]/80 mt-0.5">
            Suivez l'évolution des indices de la BRVM, inspectez les carnets d'ordres et analysez les cours en temps réel.
          </p>
        </div>
        <button 
          onClick={handleRefresh}
          className="bg-white border border-[#dec1af] text-[#574235] px-4 py-2.5 rounded-lg font-sans font-bold text-[13px] flex items-center gap-2 hover:bg-[#eff4ff] active:scale-95 transition-all shadow-xs shrink-0 self-start sm:self-center"
        >
          <RefreshCw className="w-4 h-4 animate-spin-hover" />
          Actualiser Flux
        </button>
      </div>

      {/* Stock Indexes Summary Tickers */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        {/* BRVM 30 */}
        <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 hover:shadow-sm transition-all">
          <div className="flex justify-between items-start">
            <span className="font-sans font-bold text-[11px] text-[#574235]/70 uppercase tracking-wider">BRVM 30</span>
            <span className="text-[#006d31] font-sans font-black text-[11px] bg-[#8bf6a1]/25 px-2 py-0.5 rounded-md flex items-center gap-0.5">
              <ArrowUpRight className="w-3 h-3" /> +1.45%
            </span>
          </div>
          <h3 className="font-sans font-black text-[24px] text-[#0b1c30] mt-2">108.42</h3>
          <p className="text-[11px] text-[#574235]/70 font-sans mt-1">Vol: 450.8M FCFA</p>
        </div>

        {/* BRVM Prestige */}
        <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 hover:shadow-sm transition-all">
          <div className="flex justify-between items-start">
            <span className="font-sans font-bold text-[11px] text-[#574235]/70 uppercase tracking-wider">BRVM Prestige</span>
            <span className="text-[#ba1a1a] font-sans font-black text-[11px] bg-rose-50 px-2 py-0.5 rounded-md flex items-center gap-0.5">
              <ArrowDownRight className="w-3 h-3" /> -0.22%
            </span>
          </div>
          <h3 className="font-sans font-black text-[24px] text-[#0b1c30] mt-2">101.15</h3>
          <p className="text-[11px] text-[#574235]/70 font-sans mt-1">Vol: 182.4M FCFA</p>
        </div>

        {/* BRVM Composite */}
        <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 hover:shadow-sm transition-all">
          <div className="flex justify-between items-start">
            <span className="font-sans font-bold text-[11px] text-[#574235]/70 uppercase tracking-wider">BRVM Composite</span>
            <span className="text-[#006d31] font-sans font-black text-[11px] bg-[#8bf6a1]/25 px-2 py-0.5 rounded-md flex items-center gap-0.5">
              <ArrowUpRight className="w-3 h-3" /> +0.89%
            </span>
          </div>
          <h3 className="font-sans font-black text-[24px] text-[#0b1c30] mt-2">214.30</h3>
          <p className="text-[11px] text-[#574235]/70 font-sans mt-1">Vol: 1,245.1M FCFA</p>
        </div>
      </div>

      {/* Main Column Grid */}
      <div className="grid grid-cols-12 gap-6">
        
        {/* Left Column: Interactive Stock List (Span 7) */}
        <div className="col-span-12 lg:col-span-7 bg-white border border-[#dec1af]/30 rounded-xl overflow-hidden shadow-xs flex flex-col justify-between">
          <div>
            <div className="p-5 border-b border-[#dec1af]/20 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <h3 className="font-sans font-bold text-[16px] text-[#0b1c30] flex items-center gap-2">
                <BarChart4 className="text-[#ff8200] w-5 h-5" />
                Actions List & Cotations
              </h3>
              
              {/* Internal Search Box */}
              <div className="relative pt-2 sm:pt-0">
                <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[#574235]/50" />
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Filtrer ticker..."
                  className="bg-white border border-[#dec1af]/30 rounded-lg pl-9 pr-4 py-1.5 text-[12px] font-sans focus:outline-none focus:ring-1 focus:ring-[#ff8200] outline-none w-44"
                />
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-[#f8f9ff] text-[#574235] font-sans font-bold text-[10px] uppercase tracking-wider border-b border-[#dec1af]/20">
                    <th className="px-5 py-3">Ticker</th>
                    <th className="px-5 py-3">Prix (FCFA)</th>
                    <th className="px-5 py-3 text-right">Var %</th>
                    <th className="px-5 py-3 text-center">Tendance</th>
                    <th className="px-5 py-3 text-right">Volume (FCFA)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#dec1af]/15">
                  {filteredQuotes.map((q) => {
                    const isSelected = q.ticker === selectedTicker;
                    const isPositive = q.change >= 0;
                    return (
                      <tr 
                        key={q.ticker}
                        onClick={() => setSelectedTicker(q.ticker)}
                        className={`cursor-pointer transition-colors ${
                          isSelected ? 'bg-[#ffdcc6]/20 font-bold' : 'hover:bg-[#f8f9ff]/50'
                        }`}
                      >
                        <td className="px-5 py-4">
                          <span className="font-sans font-extrabold text-[13px] text-[#0b1c30] block">{q.ticker}</span>
                          <span className="font-sans text-[11px] text-[#574235]/65 block font-medium">{q.name}</span>
                        </td>
                        <td className="px-5 py-4 font-mono text-[13px] text-[#0b1c30]">
                          {formatAmount(q.price)}
                        </td>
                        <td className={`px-5 py-4 text-right font-sans text-[12px] ${
                          isPositive ? 'text-[#006d31]' : 'text-[#ba1a1a]'
                        }`}>
                          {isPositive ? '+' : ''}{q.change}%
                        </td>
                        {/* Micro sparkline */}
                        <td className="px-5 py-4">
                          <div className="flex justify-center">
                            <svg className="w-20 h-6 overflow-visible" viewBox="0 0 100 30">
                              <polyline
                                fill="none"
                                stroke={isPositive ? '#006d31' : '#ba1a1a'}
                                strokeWidth="1.8"
                                points={q.sparkline.map((val, idx) => {
                                  const x = (idx / (q.sparkline.length - 1)) * 100;
                                  // Map min-max values to height range 3 to 27
                                  const min = Math.min(...q.sparkline);
                                  const max = Math.max(...q.sparkline);
                                  const denom = max - min || 1;
                                  const y = 27 - ((val - min) / denom) * 24;
                                  return `${x},${y}`;
                                }).join(' ')}
                              />
                            </svg>
                          </div>
                        </td>
                        <td className="px-5 py-4 text-right font-mono text-[12px] text-[#574235]">
                          {formatAmount(q.volume)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>

          <div className="p-4 bg-[#f8f9ff] border-t border-[#dec1af]/15 text-[11px] text-[#574235]/70 font-sans text-center">
            Sélectionnez une ligne pour afficher l'analyse technique et le carnet d'ordres à droite.
          </div>
        </div>

        {/* Right Column: Order Book & Technical Sparkline Analyzer (Span 5) */}
        <div className="col-span-12 lg:col-span-5 space-y-6">
          
          {/* Active Asset Chart Analysis Card */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 hover:shadow-sm transition-all duration-200">
            <div className="flex justify-between items-center mb-4">
              <div>
                <span className="bg-[#ffdcc6] text-[#954a00] px-2.5 py-0.5 rounded text-[10px] font-extrabold uppercase">
                  Analyse Technique
                </span>
                <h4 className="font-sans font-extrabold text-[18px] text-[#0b1c30] mt-1.5">
                  {currentQuote.name} ({currentQuote.ticker})
                </h4>
              </div>
              <div className="flex gap-1">
                {(['1D', '1W', '1M', '1Y'] as const).map((r) => (
                  <button
                    key={r}
                    onClick={() => setTimeRange(r)}
                    className={`px-2 py-1 rounded font-sans font-bold text-[10px] transition-all ${
                      timeRange === r 
                        ? 'bg-[#ff8200] text-white' 
                        : 'bg-[#eff4ff] text-[#574235] hover:text-[#954a00]'
                    }`}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>

            {/* Simulated interactive line graph of the quote price */}
            <div className="h-44 bg-[#f8f9ff] rounded-xl border border-[#dec1af]/20 p-4 relative overflow-hidden flex flex-col justify-between">
              <div className="flex justify-between items-start">
                <span className="font-sans font-black text-[22px] text-[#0b1c30] leading-none">
                  {formatAmount(currentQuote.price)} <span className="text-[12px] font-bold text-[#574235]">FCFA</span>
                </span>
                <span className={`font-sans text-[12px] font-bold ${
                  currentQuote.change >= 0 ? 'text-[#006d31]' : 'text-red-600'
                }`}>
                  {currentQuote.change >= 0 ? '+' : ''}{currentQuote.change}%
                </span>
              </div>

              {/* Vector SVG line graph */}
              <div className="w-full h-24 relative overflow-visible mt-2">
                <svg className="w-full h-full" preserveAspectRatio="none" viewBox="0 0 100 40">
                  <defs>
                    <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#ff8200" stopOpacity="0.25" />
                      <stop offset="100%" stopColor="#ff8200" stopOpacity="0.0" />
                    </linearGradient>
                  </defs>
                  
                  {/* Fill area below graph line */}
                  <path
                    fill="url(#chartGradient)"
                    d={`M 0,40 ` + currentQuote.sparkline.map((val, idx) => {
                      const x = (idx / (currentQuote.sparkline.length - 1)) * 100;
                      const min = Math.min(...currentQuote.sparkline);
                      const max = Math.max(...currentQuote.sparkline);
                      const denom = max - min || 1;
                      const y = 35 - ((val - min) / denom) * 30;
                      return `L ${x},${y}`;
                    }).join(' ') + ` L 100,40 Z`}
                  />

                  {/* Main stroke path */}
                  <polyline
                    fill="none"
                    stroke="#ff8200"
                    strokeWidth="2.5"
                    points={currentQuote.sparkline.map((val, idx) => {
                      const x = (idx / (currentQuote.sparkline.length - 1)) * 100;
                      const min = Math.min(...currentQuote.sparkline);
                      const max = Math.max(...currentQuote.sparkline);
                      const denom = max - min || 1;
                      const y = 35 - ((val - min) / denom) * 30;
                      return `${x},${y}`;
                    }).join(' ')}
                  />
                  {/* Indicator dot at last point */}
                  <circle
                    cx="100"
                    cy={35 - ((currentQuote.sparkline[currentQuote.sparkline.length - 1] - Math.min(...currentQuote.sparkline)) / (Math.max(...currentQuote.sparkline) - Math.min(...currentQuote.sparkline) || 1)) * 30}
                    r="4.5"
                    fill="#ff8200"
                    stroke="white"
                    strokeWidth="1.5"
                  />
                </svg>
              </div>

              <div className="flex justify-between text-[10px] text-[#574235]/70 font-sans font-semibold pt-1 border-t border-[#dec1af]/10 mt-1">
                <span>Début Oct</span>
                <span>Milieu Oct</span>
                <span>Aujourd'hui</span>
              </div>
            </div>
            
            {/* Stock Metadata stats */}
            <div className="grid grid-cols-2 gap-4 mt-4 font-sans text-[12px]">
              <div className="bg-[#f8f9ff] p-2.5 rounded-lg border border-[#dec1af]/15">
                <span className="text-[#574235]/70 block font-bold text-[10px] uppercase">Plus haut</span>
                <span className="font-mono font-bold text-[#0b1c30] mt-0.5 block">{formatAmount(currentQuote.high)} FCFA</span>
              </div>
              <div className="bg-[#f8f9ff] p-2.5 rounded-lg border border-[#dec1af]/15">
                <span className="text-[#574235]/70 block font-bold text-[10px] uppercase">Plus bas</span>
                <span className="font-mono font-bold text-[#0b1c30] mt-0.5 block">{formatAmount(currentQuote.low)} FCFA</span>
              </div>
            </div>
          </div>

          {/* Real-time Order Book Card ("Carnet d'Ordres") */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 hover:shadow-sm transition-all duration-200">
            <h4 className="font-sans font-bold text-[15px] text-[#0b1c30] mb-4 flex items-center gap-2">
              <BookOpen className="text-[#006d31] w-4.5 h-4.5" />
              Carnet d'Ordres ({currentQuote.ticker})
            </h4>

            {/* Dual book layout */}
            <div className="grid grid-cols-2 gap-4">
              
              {/* Buy Orders Book */}
              <div className="space-y-2">
                <span className="text-[11px] font-sans font-bold text-[#006d31] block border-b border-[#006d31]/15 pb-1">Achat (Demandes)</span>
                <div className="space-y-1">
                  {orderBook.buys.map((b, i) => (
                    <div key={i} className="relative py-1 px-1.5 flex justify-between text-[11px] font-sans overflow-hidden rounded">
                      <div className="absolute top-0 right-0 h-full bg-[#8bf6a1]/20 rounded-r" style={{ width: `${b.percent}%` }}></div>
                      <span className="font-mono font-bold text-[#006d31] relative z-10">{formatAmount(Math.round(b.price))}</span>
                      <span className="font-mono text-[#0b1c30] font-medium relative z-10">{b.qty}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Sell Orders Book */}
              <div className="space-y-2">
                <span className="text-[11px] font-sans font-bold text-[#ba1a1a] block border-b border-[#ba1a1a]/15 pb-1">Vente (Offres)</span>
                <div className="space-y-1">
                  {orderBook.sells.map((s, i) => (
                    <div key={i} className="relative py-1 px-1.5 flex justify-between text-[11px] font-sans overflow-hidden rounded">
                      <div className="absolute top-0 right-0 h-full bg-rose-50 rounded-r" style={{ width: `${s.percent}%` }}></div>
                      <span className="font-mono font-bold text-[#ba1a1a] relative z-10">{formatAmount(Math.round(s.price))}</span>
                      <span className="font-mono text-[#0b1c30] font-medium relative z-10">{s.qty}</span>
                    </div>
                  ))}
                </div>
              </div>

            </div>
          </div>

        </div>

      </div>

    </div>
  );
};
