const fs = require('fs');
const { spawn } = require('child_process');

const mcpConfig = {
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main"
      ]
    },
    "git": {
      "command": "npx",
      "args": [
        "-y",
        "@cyanheads/git-mcp-server"
      ]
    },
    "mysql": {
      "command": "python",
      "args": [
        "-m",
        "mysql_mcp_server_pro"
      ],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "123456",
        "MYSQL_DATABASE": "smart_canteen"
      }
    },
    "redis": {
      "command": "npx",
      "args": [
        "-y",
        "@mcpflow.io/mcp-redis",
        "redis://localhost:6379"
      ]
    },
    "swagger": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-swagger-server"
      ],
      "env": {
        "API_URL": "http://localhost:8081/v3/api-docs"
      }
    }
  }
};

async function testServer(name, config) {
    return new Promise((resolve) => {
        console.log(`\n--- Testing ${name} ---`);
        const proc = spawn(config.command, config.args, {
            env: { ...process.env, ...config.env },
            shell: true
        });

        let output = '';
        let error = '';
        let resolved = false;
        
        proc.stdout.on('data', data => {
            output += data.toString();
            if (output.includes('jsonrpc') && !resolved) {
                resolved = true;
                proc.kill();
                resolve(`SUCCESS: ${name} responded with JSON-RPC`);
            }
        });
        
        proc.stderr.on('data', data => {
            error += data.toString();
        });
        
        proc.on('close', code => {
            if (!resolved) {
                resolved = true;
                resolve(`FAILED: ${name} exited with code ${code}.\nStderr:\n${error.slice(0, 500)}`);
            }
        });

        proc.on('error', err => {
            if (!resolved) {
                resolved = true;
                resolve(`FAILED: ${name} failed to start. Error: ${err.message}`);
            }
        });

        // Send initialize request
        const initReq = {
            jsonrpc: "2.0",
            id: 1,
            method: "initialize",
            params: {
                protocolVersion: "2024-11-05",
                capabilities: {},
                clientInfo: { name: "test-client", version: "1.0.0" }
            }
        };
        proc.stdin.write(JSON.stringify(initReq) + '\n');
        
        // Timeout after 15 seconds
        setTimeout(() => {
            if (!resolved) {
                resolved = true;
                proc.kill();
                resolve(`TIMEOUT: ${name} did not respond within 15 seconds.\nStderr: ${error.slice(0, 300)}`);
            }
        }, 15000);
    });
}

async function run() {
    let finalLog = "";
    for (const [name, config] of Object.entries(mcpConfig.mcpServers)) {
        const result = await testServer(name, config);
        finalLog += `[Result for ${name}]:\n${result}\n\n`;
        console.log(`[Result for ${name}]:\n${result}`);
    }
    fs.writeFileSync('mcp_output.log', finalLog, 'utf8');
}

run().then(() => {
    console.log("All tests completed.");
    process.exit(0);
}).catch(err => {
    console.error(err);
    process.exit(1);
});
