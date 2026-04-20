import { useState } from "react";
import api from "../api";

export default function Login() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [msg, setMsg] = useState("");

  const login = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post("/api/auth/login", { username, password });
      localStorage.setItem("token", res.data.token);
      window.location.href = "/dashboard";
    } catch (err) {
      setMsg(err?.response?.data?.message || "Login failed");
    }
  };

  return (
    <form onSubmit={login}>
      <h3>Login</h3>
      <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="username" /><br /><br />
      <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" /><br /><br />
      <button type="submit">Login</button>
      <p style={{ color: "red" }}>{msg}</p>
    </form>
  );
}