import http from "node:http";

const PROXY_PORT = 3000;
const BACKEND_PORT = 8080;

const server = http.createServer((req, res) => {
  const target = `http://127.0.0.1:${BACKEND_PORT}`;
  const proxyReq = http.request(
    target + req.url,
    { method: req.method, headers: { ...req.headers, host: `localhost:${BACKEND_PORT}` } },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode, proxyRes.headers);
      proxyRes.pipe(res);
    }
  );
  proxyReq.on("error", (err) => {
    console.error("Proxy error:", err.message);
    res.writeHead(502);
    res.end("Bad Gateway");
  });
  req.pipe(proxyReq);
});

server.listen(PROXY_PORT, "0.0.0.0", () => {
  console.log(`Proxy :${PROXY_PORT} → backend :${BACKEND_PORT}`);
});
