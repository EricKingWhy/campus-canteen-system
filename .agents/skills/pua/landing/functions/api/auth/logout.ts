export const onRequestPost: PagesFunction = async () => {
  return new Response(null, {
    status: 302,
    headers: {
      Location: "/#/contribute",
      "Set-Cookie": "pua_session=; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=0",
    },
  })
}
