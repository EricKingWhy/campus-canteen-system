const puppeteer = require('puppeteer');
const path = require('path');
const fs = require('fs');

(async () => {
    const browser = await puppeteer.launch({
        headless: 'new',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();

    // 收集控制台消息
    const consoleMessages = [];
    page.on('console', msg => {
        consoleMessages.push({
            type: msg.type(),
            text: msg.text()
        });
    });

    // 收集页面错误
    const pageErrors = [];
    page.on('pageerror', error => {
        pageErrors.push(error.toString());
    });

    // 收集请求失败
    const failedRequests = [];
    page.on('requestfailed', request => {
        failedRequests.push({
            url: request.url(),
            failure: request.failure().errorText
        });
    });

    try {
        console.log('正在导航到 http://localhost:5173...');
        await page.goto('http://localhost:5173', {
            waitUntil: 'networkidle2',
            timeout: 30000
        });

        // 等待一下确保页面完全加载
        await new Promise(resolve => setTimeout(resolve, 2000));

        // 截图
        const screenshotPath = path.join(__dirname, 'screenshot.png');
        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });
        console.log(`截图已保存到: ${screenshotPath}`);

        // 获取页面标题
        const title = await page.title();

        // 生成报告
        let report = '=== Puppeteer 测试报告 ===\n';
        report += `URL: http://localhost:5173\n`;
        report += `页面标题: ${title}\n`;
        report += `截图路径: ${screenshotPath}\n\n`;

        report += '=== 控制台消息 ===\n';
        if (consoleMessages.length === 0) {
            report += '没有控制台消息\n';
        } else {
            consoleMessages.forEach((msg, index) => {
                report += `[${index + 1}] [${msg.type.toUpperCase()}] ${msg.text}\n`;
            });
        }

        report += '\n=== 页面错误 ===\n';
        if (pageErrors.length === 0) {
            report += '没有页面错误\n';
        } else {
            pageErrors.forEach((error, index) => {
                report += `[${index + 1}] ${error}\n`;
            });
        }

        report += '\n=== 失败的请求 ===\n';
        if (failedRequests.length === 0) {
            report += '没有失败的请求\n';
        } else {
            failedRequests.forEach((req, index) => {
                report += `[${index + 1}] ${req.url}\n`;
                report += `    错误: ${req.failure}\n`;
            });
        }

        // 保存报告到文件
        const reportPath = path.join(__dirname, 'puppeteer-report.txt');
        fs.writeFileSync(reportPath, report, 'utf8');

        // 同时输出到控制台
        console.log(report);
        console.log(`\n报告已保存到: ${reportPath}`);

    } catch (error) {
        console.error('发生错误:', error.message);
    } finally {
        await browser.close();
    }
})();
