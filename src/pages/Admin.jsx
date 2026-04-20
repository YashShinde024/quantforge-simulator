import { useState } from "react";
import api from "../api";

export default function Admin() {
  const [symbol, setSymbol] = useState("AAPL");
  const [price, setPrice] = useState("210");
  const [msg, setMsg] = useState("");

  const update = async () => {
    try {
      await api.post("/api/trading/admin/price", { symbol, price: Number(price) });
      setMsg("Price updated");
    } catch (e) {
      setMsg(e?.response?.data?.message || "Failed");
    }
  };

  return (
    <div>
      <h3>Admin</h3>
      <input value={symbol} onChange={(e) => setSymbol(e.target.value)} />
      <input value={price} onChange={(e) => setPrice(e.target.value)} />
      <button onClick={update}>Update Price</button>
      <p>{msg}</p>
    </div>
  );
}