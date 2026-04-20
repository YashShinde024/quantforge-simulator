import { useEffect, useState } from "react";
import api from "../api";

export default function Dashboard() {
  const [wallet, setWallet] = useState(null);
  const [assets, setAssets] = useState([]);

  useEffect(() => {
    api.get("/api/trading/wallet").then(r => setWallet(r.data));
    api.get("/api/trading/assets").then(r => setAssets(r.data));
  }, []);

  return (
    <div>
      <h3>Dashboard</h3>
      <p><b>Wallet:</b> {wallet ? wallet.balance : "Loading..."}</p>
      <h4>Assets</h4>
      <ul>
        {assets.map(a => <li key={a.id}>{a.symbol} - {a.currentPrice}</li>)}
      </ul>
    </div>
  );
}