import { useCallback } from 'react';

const REST_PATH = '/polarion/api-extender/rest';

interface RequestParams {
  method: string;
  url: string;
  // BodyInit (not just string) so this hook satisfies react-sbb-polarion's SendRequest type structurally.
  body?: BodyInit;
  contentType?: string;
}

export default function useRemote() {
  const sendRequest = useCallback(({ method, url, body, contentType }: RequestParams): Promise<Response> => {
    const headers: Record<string, string> = {};
    if (contentType) {
      headers['Content-Type'] = contentType;
    }
    if (import.meta.env.VITE_BEARER_TOKEN) {
      headers['Authorization'] = `Bearer ${import.meta.env.VITE_BEARER_TOKEN}`;
    }

    const apiPath = import.meta.env.VITE_BEARER_TOKEN ? '/api' : '/internal';

    return fetch(`${REST_PATH}${apiPath}${url}`, {
      method,
      mode: 'cors',
      cache: 'no-cache',
      headers,
      body,
    }).catch(() => {
      return new Response(
        JSON.stringify({ message: 'Network error occurred. Be sure Polarion is started and accessible.' }),
        { status: 503, headers: { 'Content-Type': 'application/json' } },
      );
    });
  }, []);

  return { sendRequest };
}
