interface ApiErrorPayload {
  message?: string
  fieldErrors?: Record<string, string>
}

export class ApiError extends Error {
  fieldErrors: Record<string, string>

  constructor(message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.fieldErrors = fieldErrors
  }
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    credentials: 'include',
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    const payload = (await response.json().catch(() => ({}))) as ApiErrorPayload
    throw new ApiError(payload.message ?? 'The request could not be completed', payload.fieldErrors)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}
