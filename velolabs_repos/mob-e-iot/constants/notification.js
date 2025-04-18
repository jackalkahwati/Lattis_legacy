require("isomorphic-fetch");

const notificationRequest = async (
  body,
  notificationURL,
  apiClient,
  apiKey
) => {
  const res = await fetch(notificationURL, {
    method: "POST",
    headers: {
      "x-api-client": apiClient,
      "x-api-key": apiKey,
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
  return await res.json();
};

module.exports = { notificationRequest };
