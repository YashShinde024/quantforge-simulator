import { Link, Navigate, Route, Routes } from "react-router-dom";

function Login() {
  return <h3>Login page</h3>;
}
function Dashboard() {
  return <h3>Dashboard page</h3>;
}
function Trade() {
  return <h3>Trade page</h3>;
}
function Orders() {
  return <h3>Orders page</h3>;
}
function Admin() {
  return <h3>Admin page</h3>;
}

function PrivateRoute({ children }) {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  const logout = () => {
    localStorage.removeItem("token");
    window.location.href = "/login";
  };

  return (
    <div style={{ fontFamily: "Arial", padding: 16 }}>
      <h2>QuantForge Frontend</h2>

      <nav style={{ display: "flex", gap: 12, marginBottom: 16 }}>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/trade">Trade</Link>
        <Link to="/orders">Orders</Link>
        <Link to="/admin">Admin</Link>
        <button onClick={logout}>Logout</button>
      </nav>

      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/trade" element={<PrivateRoute><Trade /></PrivateRoute>} />
        <Route path="/orders" element={<PrivateRoute><Orders /></PrivateRoute>} />
        <Route path="/admin" element={<PrivateRoute><Admin /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </div>
  );
}