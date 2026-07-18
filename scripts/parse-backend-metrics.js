const fs = require('fs');
const path = require('path');

const surefireDir = path.resolve('target/surefire-reports');
const newmanReport = path.resolve('reports/newman-report.json');
const outputCsv = path.resolve('backend-metrics-governance.csv');
const outputJson = path.resolve('backend-metrics-governance.json');

function parseAttributes(text) {
  const attrs = {};
  for (const match of text.matchAll(/(\w+)="([^"]*)"/g)) {
    attrs[match[1]] = match[2];
  }
  return attrs;
}

function parseSurefireFiles() {
  if (!fs.existsSync(surefireDir)) return { tests: 0, failures: 0, errors: 0, skipped: 0, classes: 0 };

  const files = fs.readdirSync(surefireDir).filter(name => name.endsWith('.xml'));
  let tests = 0;
  let failures = 0;
  let errors = 0;
  let skipped = 0;

  for (const file of files) {
    const content = fs.readFileSync(path.join(surefireDir, file), 'utf8');
    const suiteMatch = content.match(/<testsuite\b([^>]*)>/);
    if (!suiteMatch) continue;
    const attrs = parseAttributes(suiteMatch[1]);
    tests += Number(attrs.tests || 0);
    failures += Number(attrs.failures || 0);
    errors += Number(attrs.errors || 0);
    skipped += Number(attrs.skipped || 0);
  }

  return {
    tests,
    failures,
    errors,
    skipped,
    classes: files.length
  };
}

function parseNewmanReport() {
  if (!fs.existsSync(newmanReport)) return { total: 0, failed: 0, passed: 0 };

  const content = fs.readFileSync(newmanReport, 'utf8');
  const data = JSON.parse(content);
  const assertStats = data.run?.stats?.assertions || {};
  const total = Number(assertStats.total || 0);
  const failed = Number(assertStats.failed || 0);
  return {
    total,
    failed,
    passed: total - failed
  };
}

function writeCsv(metrics) {
  const rows = [
    ['metric', 'value', 'unit', 'description'],
    ['coverage_percent', metrics.coverage_percent, 'percent', 'Cobertura total de líneas ejecutadas por JUnit'],
    ['unit_test_success_rate_percent', metrics.unit_test_success_rate_percent, 'percent', 'Tasa de éxito de pruebas unitarias'],
    ['defect_density_unitarias', metrics.defect_density_unitarias, 'defectos_por_clase', 'Densidad de defectos unitarias por clase probada'],
    ['api_pass_rate_percent', metrics.api_pass_rate_percent, 'percent', 'Tasa de éxito API en Newman'],
    ['api_defect_density', metrics.api_defect_density, 'defectos_por_assertion', 'Densidad de defectos en las aserciones de Newman'],
    ['pipeline_lead_time_seconds', metrics.pipeline_lead_time_seconds, 'seconds', 'Duración del pipeline desde su inicio hasta el final del script']
  ];

  const csv = rows.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\n');
  fs.writeFileSync(outputCsv, csv, 'utf8');
}

function main() {
  const surefire = parseSurefireFiles();
  const newman = parseNewmanReport();

  const coveragePath = path.resolve('target/site/jacoco/jacoco.xml');
  let coveragePercent = 0;
  if (fs.existsSync(coveragePath)) {
    const xml = fs.readFileSync(coveragePath, 'utf8');
    const lineMatch = xml.match(/<counter type="LINE" missed="(\d+)" covered="(\d+)"\/>/);
    if (lineMatch) {
      const missed = Number(lineMatch[1]);
      const covered = Number(lineMatch[2]);
      coveragePercent = total = covered + missed ? Number(((covered / (covered + missed)) * 100).toFixed(2)) : 0;
    }
  }

  const totalTests = surefire.tests;
  const totalFailed = surefire.failures + surefire.errors;
  const passedTests = Math.max(0, totalTests - totalFailed - surefire.skipped);

  const unitTestSuccessRate = totalTests
    ? Number(((passedTests / totalTests) * 100).toFixed(2))
    : 0;

  const defectDensityUnitarias = surefire.classes
    ? Number((totalFailed / surefire.classes).toFixed(4))
    : 0;

  const apiPassRate = newman.total
    ? Number(((newman.passed / newman.total) * 100).toFixed(2))
    : 0;

  const apiDefectDensity = newman.total
    ? Number((newman.failed / newman.total).toFixed(4))
    : 0;

  const startTs = Number(process.env.WORKFLOW_START_TS || '0');
  const finishTs = Math.floor(Date.now() / 1000);
  const leadTimeSeconds = startTs ? finishTs - startTs : 0;

  const metrics = {
    coverage_percent: coveragePercent,
    unit_test_success_rate_percent: unitTestSuccessRate,
    defect_density_unitarias: defectDensityUnitarias,
    api_pass_rate_percent: apiPassRate,
    api_defect_density: apiDefectDensity,
    pipeline_lead_time_seconds: leadTimeSeconds,
    surefire_tests_total: totalTests,
    surefire_failures_total: totalFailed,
    surefire_skipped_total: surefire.skipped,
    surefire_classes_total: surefire.classes,
    newman_assertions_total: newman.total,
    newman_assertions_failed: newman.failed
  };

  fs.writeFileSync(outputJson, JSON.stringify(metrics, null, 2), 'utf8');
  writeCsv(metrics);

  console.log('Generated', outputCsv);
  console.log('Generated', outputJson);
  console.log(metrics);
}

main();