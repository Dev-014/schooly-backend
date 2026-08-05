async function test() {
  try {
    const res = await fetch("http://localhost:8080/api/v1/users/grant-access", {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + (process.env.TEST_TOKEN || '')
      },
      body: JSON.stringify({ entityId: 18, entityType: "STUDENT" })
    });
    console.log(res.status);
    console.log(await res.text());
  } catch(e) {
    console.error(e);
  }
}
test();
