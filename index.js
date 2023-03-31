import { Octokit, App } from "octokit";
import * as dotenv from 'dotenv'; // see https://github.com/motdotla/dotenv#how-do-i-use-dotenv-with-import
dotenv.config();
import { writeFileSync, existsSync, mkdirSync } from 'fs';
import YAML from 'yaml';

// Octokit.js
// https://github.com/octokit/core.js#readme
const octokit = new Octokit({
    auth: process.env.GITHUB_TOKEN
})

if (!existsSync("./results"))
    mkdirSync("./results");
let currentPage = process.argv[2] || 1;
let continueFlag = true;
while (continueFlag) {
    let result = undefined;
    try {
        console.log("Current page: " + currentPage);
        result = await octokit.rest.search.code({
            q: 'swagger openapi paths+in:file+language:yml',
            per_page: 100,
            page: currentPage
        });
    }
    catch(e) {
        console.error(e);
        continueFlag = false;
        break;
    }
    if (result && "data" in result && "items" in result.data) {
        const items = result.data.items;
        for (const i in items) {
            try {
                const item = items[i];
                const url = item.html_url.replace("https://github.com/", "https://raw.githubusercontent.com/").replace("/blob", "");
                const response = await fetch(url);
                const text = await response.text();
                const documents = YAML.parseAllDocuments(text);
                if (documents && documents.length) {
                    for (const i in documents) {
                        const document = documents[i];
                        if ((document.has("swagger") || document.has("openapi"))&& document.has("paths")) {
                            const filename = "./results/" + item.repository.full_name.replaceAll("/", "_") + "_" + i + "_" + item.name;
                            console.log(filename);
                            writeFileSync(filename, document.toString());
                        }
                    }
                }
            }
            catch(e) {
                console.error(e);
            }
        }
        currentPage++;
    }
    await new Promise(resolve => setTimeout(resolve, 30000));
}